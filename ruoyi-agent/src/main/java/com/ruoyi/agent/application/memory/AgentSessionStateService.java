package com.ruoyi.agent.application.memory;

import com.ruoyi.agent.domain.runtime.AgentSessionState;
import com.ruoyi.agent.mapper.runtime.AgentSessionStateMapper;
import com.ruoyi.common.exception.ServiceException;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 维护会话级业务工作记忆，所有修改使用行锁和版本号。 */
@Service
public class AgentSessionStateService
{
    private final AgentSessionStateMapper stateMapper;

    public AgentSessionStateService(AgentSessionStateMapper stateMapper)
    {
        this.stateMapper = stateMapper;
    }

    /** 查询当前状态；尚未产生工作记忆时返回空状态。 */
    public AgentSessionState get(Long conversationId)
    {
        AgentSessionState state = stateMapper.selectByConversationId(conversationId);
        return state == null ? newState(conversationId, "") : state;
    }

    /** 记录当前任务目标和业务域。 */
    @Transactional
    public AgentSessionState rememberGoal(Long conversationId, String goal, String domain, String username)
    {
        return mutate(conversationId, username, state -> {
            state.setActiveGoal(goal);
            state.setCurrentDomain(domain);
        });
    }

    /** 记录最近一次可复用数据集。 */
    @Transactional
    public AgentSessionState rememberDataset(Long conversationId, Long datasetId, String username)
    {
        return mutate(conversationId, username, state -> state.setLastDatasetId(datasetId));
    }

    /** 记录最近一次结构化产物。 */
    @Transactional
    public AgentSessionState rememberArtifact(Long conversationId, Long artifactId, Long fileId, String username)
    {
        return mutate(conversationId, username, state -> {
            state.setLastArtifactId(artifactId);
            if (fileId != null)
            {
                state.setLastFileId(fileId);
            }
        });
    }

    /** 保存压缩后的会话摘要。 */
    @Transactional
    public AgentSessionState rememberSummary(Long conversationId, String summary, String username)
    {
        return mutate(conversationId, username, state -> state.setConversationSummary(summary));
    }

    private AgentSessionState mutate(Long conversationId, String username, Consumer<AgentSessionState> mutation)
    {
        AgentSessionState state = stateMapper.selectForUpdate(conversationId);
        if (state == null)
        {
            state = newState(conversationId, username);
            mutation.accept(state);
            stateMapper.insert(state);
            return state;
        }
        mutation.accept(state);
        state.setUpdateBy(username);
        if (stateMapper.updateWithVersion(state) != 1)
        {
            throw new ServiceException("会话状态已被其他任务修改，请重试");
        }
        state.setStateVersion(state.getStateVersion() + 1L);
        return state;
    }

    private AgentSessionState newState(Long conversationId, String username)
    {
        AgentSessionState state = new AgentSessionState();
        state.setConversationId(conversationId);
        state.setStateVersion(0L);
        state.setCreateBy(username);
        state.setUpdateBy(username);
        return state;
    }
}
