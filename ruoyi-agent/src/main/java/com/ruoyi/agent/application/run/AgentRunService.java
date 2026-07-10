package com.ruoyi.agent.application.run;

import com.ruoyi.agent.application.AgentConversationService;
import com.ruoyi.agent.domain.enums.AgentRunStatus;
import com.ruoyi.agent.domain.runtime.AgentRun;
import com.ruoyi.agent.mapper.runtime.AgentRunMapper;
import com.ruoyi.agent.mapper.runtime.AgentRunStepMapper;
import com.ruoyi.agent.mapper.runtime.AgentToolCallMapper;
import com.ruoyi.common.exception.ServiceException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Agent V2 运行生命周期和事件序号服务。 */
@Service
public class AgentRunService
{
    private static final Duration TOOL_TOKEN_TTL = Duration.ofMinutes(30);
    private static final String SUPERVISOR_CODE = "DIFY_SUPERVISOR";

    private final AgentConversationService conversationService;
    private final AgentRunMapper runMapper;
    private final AgentRunStepMapper stepMapper;
    private final AgentToolCallMapper toolCallMapper;
    private final AgentRunTokenService tokenService;

    public AgentRunService(AgentConversationService conversationService, AgentRunMapper runMapper,
        AgentRunStepMapper stepMapper, AgentToolCallMapper toolCallMapper, AgentRunTokenService tokenService)
    {
        this.conversationService = conversationService;
        this.runMapper = runMapper;
        this.stepMapper = stepMapper;
        this.toolCallMapper = toolCallMapper;
        this.tokenService = tokenService;
    }

    /** 创建属于当前用户的运行，并返回仅本次编排可见的工具令牌。 */
    @Transactional
    public AgentRunHandle create(Long conversationId, Long userId, Long userMessageId, Long assistantMessageId,
        String username)
    {
        conversationService.requireOwned(conversationId, userId);
        String token = tokenService.generate();
        AgentRun run = new AgentRun();
        run.setConversationId(conversationId);
        run.setUserId(userId);
        run.setUserMessageId(userMessageId);
        run.setAssistantMessageId(assistantMessageId);
        run.setStatus(AgentRunStatus.CREATED.name());
        run.setActiveAgent(SUPERVISOR_CODE);
        run.setLastSequence(0L);
        run.setTokenCount(0);
        run.setToolTokenHash(tokenService.hash(token));
        run.setToolTokenExpireAt(Date.from(Instant.now().plus(TOOL_TOKEN_TTL)));
        run.setCreateBy(username);
        run.setUpdateBy(username);
        runMapper.insert(run);
        return new AgentRunHandle(run, token);
    }

    /** 更新 Dify 运行标识，供停止任务和审计使用。 */
    @Transactional
    public void updateDifyIdentifiers(Long runId, String taskId, String workflowRunId, String username)
    {
        AgentRun run = requireExistingForUpdate(runId);
        if (taskId != null && !taskId.isBlank()) run.setDifyTaskId(taskId);
        if (workflowRunId != null && !workflowRunId.isBlank()) run.setWorkflowRunId(workflowRunId);
        run.setUpdateBy(username);
        runMapper.update(run);
    }

    /** 将新运行切换到执行中。 */
    @Transactional
    public AgentRun start(Long runId, Long userId, String username)
    {
        AgentRun run = requireOwned(runId, userId);
        requireStatus(run, AgentRunStatus.CREATED);
        run.setStatus(AgentRunStatus.RUNNING.name());
        run.setStartedAt(new Date());
        run.setUpdateBy(username);
        runMapper.update(run);
        return run;
    }

    /** 以数据库行锁分配严格递增的 SSE 事件序号。 */
    @Transactional
    public long nextSequence(Long runId)
    {
        AgentRun run = requireExistingForUpdate(runId);
        long next = (run.getLastSequence() == null ? 0L : run.getLastSequence()) + 1L;
        run.setLastSequence(next);
        runMapper.update(run);
        return next;
    }

    /** 校验 Dify 工具回调是否属于仍可执行的运行。 */
    public AgentRun requireToolAuthorized(Long runId, String toolToken)
    {
        AgentRun run = requireExisting(runId);
        if (!tokenService.matches(toolToken, run.getToolTokenHash()))
        {
            throw new ServiceException("工具运行令牌无效");
        }
        if (run.getToolTokenExpireAt() == null || run.getToolTokenExpireAt().before(new Date()))
        {
            throw new ServiceException("工具运行令牌已过期");
        }
        AgentRunStatus status = AgentRunStatus.valueOf(run.getStatus());
        if (status != AgentRunStatus.CREATED && status != AgentRunStatus.RUNNING)
        {
            throw new ServiceException("当前运行状态不允许调用工具");
        }
        return run;
    }

    /** 正常完成运行并立即废止工具令牌。 */
    @Transactional
    public boolean complete(Long runId, Integer tokenCount, String username)
    {
        AgentRun run = requireExistingForUpdate(runId);
        if (AgentRunStatus.valueOf(run.getStatus()).isTerminal())
        {
            return false;
        }
        run.setStatus(AgentRunStatus.COMPLETED.name());
        run.setTokenCount(tokenCount == null ? 0 : tokenCount);
        finish(run, username);
        return true;
    }

    /** 记录受控失败并立即废止工具令牌。 */
    @Transactional
    public boolean fail(Long runId, String errorCode, String errorMessage, String username)
    {
        AgentRun run = requireExistingForUpdate(runId);
        if (AgentRunStatus.valueOf(run.getStatus()).isTerminal())
        {
            return false;
        }
        run.setStatus(AgentRunStatus.FAILED.name());
        run.setErrorCode(errorCode);
        run.setErrorMessage(errorMessage);
        finish(run, username);
        return true;
    }

    /** 取消运行，同时收敛未结束步骤和工具调用。 */
    @Transactional
    public boolean cancel(Long runId, Long userId, String username)
    {
        AgentRun run = requireOwned(runId, userId);
        if (AgentRunStatus.valueOf(run.getStatus()).isTerminal())
        {
            return false;
        }
        run.setStatus(AgentRunStatus.CANCELLED.name());
        finish(run, username);
        stepMapper.cancelRunningByRunId(runId);
        toolCallMapper.cancelRunningByRunId(runId);
        return true;
    }

    /** 查询用户拥有的运行。 */
    public AgentRun requireOwned(Long runId, Long userId)
    {
        AgentRun run = runMapper.selectOwned(runId, userId);
        if (run == null)
        {
            throw new ServiceException("Agent运行不存在或无权访问");
        }
        return run;
    }

    private void finish(AgentRun run, String username)
    {
        run.setFinishedAt(new Date());
        run.setToolTokenHash(null);
        run.setToolTokenExpireAt(new Date());
        run.setUpdateBy(username);
        runMapper.update(run);
    }

    private AgentRun requireExisting(Long runId)
    {
        AgentRun run = runMapper.selectById(runId);
        if (run == null)
        {
            throw new ServiceException("Agent运行不存在");
        }
        return run;
    }

    private AgentRun requireExistingForUpdate(Long runId)
    {
        AgentRun run = runMapper.selectForUpdate(runId);
        if (run == null)
        {
            throw new ServiceException("Agent运行不存在");
        }
        return run;
    }

    private void requireStatus(AgentRun run, AgentRunStatus expected)
    {
        if (!expected.name().equals(run.getStatus()))
        {
            throw new ServiceException("Agent运行状态不允许当前操作");
        }
    }
}
