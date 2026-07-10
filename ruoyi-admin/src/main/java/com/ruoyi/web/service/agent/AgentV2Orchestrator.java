package com.ruoyi.web.service.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.api.v2.AgentV2RunCreated;
import com.ruoyi.agent.api.v2.AgentV2RunRequest;
import com.ruoyi.agent.application.AgentConversationService;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.application.artifact.AgentV2ArtifactService;
import com.ruoyi.agent.application.memory.AgentMemoryAssembler;
import com.ruoyi.agent.application.memory.AgentSessionStateService;
import com.ruoyi.agent.application.run.AgentRunHandle;
import com.ruoyi.agent.application.run.AgentRunService;
import com.ruoyi.agent.application.source.AgentSource;
import com.ruoyi.agent.application.source.SourceExtractor;
import com.ruoyi.agent.application.stream.AgentV2EventService;
import com.ruoyi.agent.application.stream.ReasoningContentFilter;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.domain.enums.AgentRunStatus;
import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentV2ConfigKey;
import com.ruoyi.agent.domain.enums.AgentV2EventType;
import com.ruoyi.agent.domain.enums.DifyAppCode;
import com.ruoyi.agent.domain.enums.DifyEventType;
import com.ruoyi.agent.domain.enums.MessageRole;
import com.ruoyi.agent.domain.enums.MessageStatus;
import com.ruoyi.agent.domain.runtime.AgentRun;
import com.ruoyi.agent.infrastructure.dify.DifyChatflowClient;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import com.ruoyi.agent.mapper.AgentConversationMapper;
import com.ruoyi.agent.mapper.AgentMessageMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysConfigService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** 编排 Agent V2 本地状态、Dify Supervisor 流和稳定事件协议。 */
@Service
public class AgentV2Orchestrator
{
    private final AgentConversationService conversationService;
    private final AgentConversationMapper conversationMapper;
    private final AgentMessageMapper messageMapper;
    private final AgentRunService runService;
    private final AgentSessionStateService stateService;
    private final AgentMemoryAssembler memoryAssembler;
    private final AgentV2EventService eventService;
    private final AgentV2ArtifactService artifactService;
    private final SourceExtractor sourceExtractor;
    private final DifyAppConfigService appConfigService;
    private final ISysConfigService configService;
    private final DifyChatflowClient difyClient;
    private final Executor executor;
    private final ConcurrentHashMap<Long, V2StreamState> activeStreams = new ConcurrentHashMap<>();

    public AgentV2Orchestrator(AgentConversationService conversationService,
        AgentConversationMapper conversationMapper, AgentMessageMapper messageMapper, AgentRunService runService,
        AgentSessionStateService stateService, AgentMemoryAssembler memoryAssembler,
        AgentV2EventService eventService, AgentV2ArtifactService artifactService, SourceExtractor sourceExtractor,
        DifyAppConfigService appConfigService, ISysConfigService configService, DifyChatflowClient difyClient,
        @Qualifier("threadPoolTaskExecutor") Executor executor)
    {
        this.conversationService = conversationService;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.runService = runService;
        this.stateService = stateService;
        this.memoryAssembler = memoryAssembler;
        this.eventService = eventService;
        this.artifactService = artifactService;
        this.sourceExtractor = sourceExtractor;
        this.appConfigService = appConfigService;
        this.configService = configService;
        this.difyClient = difyClient;
        this.executor = executor;
    }

    /** 创建运行并立即在后台启动 Dify Supervisor。 */
    public AgentV2RunCreated create(AgentV2RunRequest request, Long userId, String username)
    {
        AgentConversation conversation = request.getConversationId() == null
            ? conversationService.create(userId, request.getQuery(), username)
            : conversationService.requireOwned(request.getConversationId(), userId);
        AgentMessage userMessage = saveMessage(conversation.getId(), MessageRole.USER, request.getQuery(),
            MessageStatus.COMPLETED, username);
        AgentMessage assistantMessage = saveMessage(conversation.getId(), MessageRole.ASSISTANT, "",
            MessageStatus.STREAMING, username);
        AgentRunHandle handle = runService.create(conversation.getId(), userId, userMessage.getId(),
            assistantMessage.getId(), username);
        AgentRun run = runService.start(handle.run().getId(), userId, username);
        eventService.publish(AgentV2EventType.RUN_CREATED, run.getId(), Map.of(
            "conversationId", conversation.getId(), "status", run.getStatus(), "title", conversation.getTitle()));
        V2StreamState stream = new V2StreamState(conversation, assistantMessage, request.getQuery());
        activeStreams.put(run.getId(), stream);
        executor.execute(() -> execute(run.getId(), request, stream, handle.toolToken(), username));
        return new AgentV2RunCreated(run.getId(), conversation.getId(), run.getStatus());
    }

    /** 取消本地 Run，并尽力停止 Dify 当前 task。 */
    public void cancel(Long runId, Long userId, String username)
    {
        AgentRun run = runService.requireOwned(runId, userId);
        if (!runService.cancel(runId, userId, username))
        {
            return;
        }
        V2StreamState stream = activeStreams.get(runId);
        if (stream != null) stream.cancelled = true;
        stopAssistantMessage(run, username, stream);
        eventService.publish(AgentV2EventType.RUN_CANCELLED, runId, Map.of("message", "用户已停止生成"));
        if (run.getDifyTaskId() != null)
        {
            try
            {
                difyClient.stop(settings(), run.getDifyTaskId(), difyUser(userId));
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            catch (IOException ignored)
            {
                // 本地取消不能被远端停止失败阻断，Dify结果到达后会因Run已终止而被忽略。
            }
        }
    }

    private void execute(Long runId, AgentV2RunRequest request, V2StreamState stream,
        String toolToken, String username)
    {
        try
        {
            Map<String, Object> inputs = memoryAssembler.assemble(runId, toolToken,
                stateService.get(stream.conversation.getId()), request.getInputs(), request.getContextRefs());
            DifyChatRequest difyRequest = new DifyChatRequest(request.getQuery(), inputs,
                stream.conversation.getDifyConversationId(), difyUser(stream.conversation.getUserId()));
            difyClient.stream(settings(), difyRequest, event -> handleDifyEvent(runId, stream, event, username));
            if (stream.cancelled) return;
            appendVisibleText(runId, stream, stream.reasoningFilter.finish());
            complete(runId, stream, username);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            fail(runId, stream, "AGENT_INTERRUPTED", "Agent运行被中断", username);
        }
        catch (Exception e)
        {
            fail(runId, stream, "AGENT_EXECUTION_FAILED", safeMessage(e), username);
        }
        finally
        {
            activeStreams.remove(runId, stream);
        }
    }

    private void handleDifyEvent(Long runId, V2StreamState stream, DifyStreamEvent event, String username)
    {
        if (stream.cancelled) return;
        updateIdentifiers(runId, stream, event, username);
        DifyEventType type = DifyEventType.fromCode(event.getEvent());
        if (type == DifyEventType.MESSAGE || type == DifyEventType.AGENT_MESSAGE)
        {
            appendVisibleText(runId, stream, stream.reasoningFilter.accept(event.getAnswer()));
        }
        else if (type == DifyEventType.MESSAGE_REPLACE)
        {
            ReasoningContentFilter filter = new ReasoningContentFilter();
            String replacement = filter.accept(event.getAnswer()) + filter.finish();
            stream.answer.setLength(0);
            stream.answer.append(replacement);
            eventService.publish(AgentV2EventType.MESSAGE_REPLACED, runId, Map.of("content", replacement));
        }
        else if (type == DifyEventType.MESSAGE_END)
        {
            stream.metadata.putAll(event.getMetadata());
            stream.tokenCount = totalTokens(event.getMetadata());
            List<AgentSource> sources = sourceExtractor.extractFromMetadata(event.getMetadata());
            for (AgentSource source : sources)
            {
                if (stream.sourceIds.add(source.getId()))
                {
                    stream.sources.add(source);
                    eventService.publish(AgentV2EventType.CITATION_CREATED, runId, citationData(source));
                }
            }
        }
        else if (type == DifyEventType.AGENT_THOUGHT)
        {
            publishExternalToolProgress(runId, stream, event);
        }
        else if (type == DifyEventType.ERROR)
        {
            throw new ServiceException(event.getMessage() == null ? "Dify Supervisor返回错误" : event.getMessage());
        }
        else if (type == DifyEventType.WORKFLOW_FINISHED
            && "failed".equals(String.valueOf(event.getData().get("status"))))
        {
            throw new ServiceException(String.valueOf(event.getData().getOrDefault("error", "Dify Supervisor执行失败")));
        }
    }

    private void publishExternalToolProgress(Long runId, V2StreamState stream, DifyStreamEvent event)
    {
        List<String> tools = toolNames(event.getRaw().get("tool"));
        if (tools.isEmpty()) return;
        String thoughtId = text(event.getRaw().get("id"));
        String position = text(event.getRaw().get("position"));
        String stepBase = !thoughtId.isBlank() ? thoughtId : "position-" + position;
        boolean completed = !text(event.getRaw().get("observation")).isBlank();
        Map<?, ?> labels = event.getRaw().get("tool_labels") instanceof Map<?, ?> map ? map : Map.of();
        for (String tool : tools)
        {
            String stepId = "dify-tool:" + stepBase + ":" + tool;
            String displayName = "正在调用 " + toolLabel(tool, labels);
            if (!completed && stream.startedDifyToolSteps.add(stepId))
            {
                eventService.publish(AgentV2EventType.STEP_STARTED, runId, Map.of(
                    "stepId", stepId,
                    "stepType", AgentStepType.REALTIME_SEARCH.name(),
                    "displayName", displayName));
                eventService.publish(AgentV2EventType.TOOL_STARTED, runId, Map.of(
                    "toolCallId", stepId,
                    "toolCode", tool,
                    "stepId", stepId));
            }
            if (completed && stream.completedDifyToolSteps.add(stepId))
            {
                if (stream.startedDifyToolSteps.add(stepId))
                {
                    eventService.publish(AgentV2EventType.STEP_STARTED, runId, Map.of(
                        "stepId", stepId,
                        "stepType", AgentStepType.REALTIME_SEARCH.name(),
                        "displayName", displayName));
                }
                String summary = "已获取 " + toolLabel(tool, labels) + " 结果";
                eventService.publish(AgentV2EventType.TOOL_COMPLETED, runId, Map.of(
                    "toolCallId", stepId,
                    "toolCode", tool,
                    "stepId", stepId,
                    "summary", summary));
                eventService.publish(AgentV2EventType.STEP_COMPLETED, runId, Map.of(
                    "stepId", stepId,
                    "stepType", AgentStepType.REALTIME_SEARCH.name(),
                    "summary", summary));
            }
        }
    }

    private List<String> toolNames(Object value)
    {
        String toolText = text(value);
        if (toolText.isBlank()) return List.of();
        List<String> tools = new ArrayList<>();
        for (String part : toolText.split("[;,]"))
        {
            String tool = part.trim();
            if (!tool.isBlank()) tools.add(tool);
        }
        return tools;
    }

    private String toolLabel(String tool, Map<?, ?> labels)
    {
        String alias = toolDisplayAliases().get(tool);
        if (alias != null) return alias;
        Object labelValue = labels.get(tool);
        if (labelValue instanceof Map<?, ?> labelMap)
        {
            Object zh = labelMap.get("zh_Hans");
            if (zh != null && !String.valueOf(zh).isBlank()) return String.valueOf(zh);
            Object en = labelMap.get("en_US");
            if (en != null && !String.valueOf(en).isBlank()) return String.valueOf(en);
        }
        return tool;
    }

    private Map<String, String> toolDisplayAliases()
    {
        String value = configService.selectConfigByKey(AgentV2ConfigKey.TOOL_DISPLAY_ALIASES.getKey());
        if (value == null || value.isBlank()) return Map.of();
        Map<String, String> aliases = new LinkedHashMap<>();
        for (String item : value.split("[,\\n;]"))
        {
            String pair = item.trim();
            if (pair.isBlank()) continue;
            int delimiter = pair.indexOf('=');
            if (delimiter < 1) delimiter = pair.indexOf(':');
            if (delimiter < 1) continue;
            String key = pair.substring(0, delimiter).trim();
            String label = pair.substring(delimiter + 1).trim();
            if (!key.isBlank() && !label.isBlank()) aliases.put(key, label);
        }
        return aliases;
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void updateIdentifiers(Long runId, V2StreamState stream, DifyStreamEvent event, String username)
    {
        boolean runChanged = false;
        boolean messageChanged = false;
        boolean conversationChanged = false;
        if (event.getTaskId() != null && !event.getTaskId().equals(stream.taskId))
        {
            stream.taskId = event.getTaskId();
            stream.message.setTaskId(event.getTaskId());
            stream.conversation.setLastTaskId(event.getTaskId());
            runChanged = true;
            messageChanged = true;
            conversationChanged = true;
        }
        if (event.getWorkflowRunId() != null && !event.getWorkflowRunId().equals(stream.workflowRunId))
        {
            stream.workflowRunId = event.getWorkflowRunId();
            stream.message.setWorkflowRunId(event.getWorkflowRunId());
            stream.conversation.setLastWorkflowRunId(event.getWorkflowRunId());
            runChanged = true;
            messageChanged = true;
            conversationChanged = true;
        }
        if (event.getMessageId() != null && !event.getMessageId().equals(stream.message.getDifyMessageId()))
        {
            stream.message.setDifyMessageId(event.getMessageId());
            messageChanged = true;
        }
        if (event.getConversationId() != null
            && !event.getConversationId().equals(stream.conversation.getDifyConversationId()))
        {
            stream.conversation.setDifyConversationId(event.getConversationId());
            conversationChanged = true;
        }
        if (runChanged) runService.updateDifyIdentifiers(runId, stream.taskId, stream.workflowRunId, username);
        if (messageChanged) messageMapper.update(stream.message);
        if (conversationChanged)
        {
            stream.conversation.setUpdateBy(username);
            conversationMapper.update(stream.conversation);
        }
    }

    private void appendVisibleText(Long runId, V2StreamState stream, String text)
    {
        if (text == null || text.isEmpty()) return;
        stream.answer.append(text);
        eventService.publish(AgentV2EventType.MESSAGE_DELTA, runId, Map.of("content", text));
    }

    private void complete(Long runId, V2StreamState stream, String username)
    {
        if (!runService.complete(runId, stream.tokenCount, username))
        {
            return;
        }
        stream.message.setContent(stream.answer.toString());
        stream.message.setStatus(MessageStatus.COMPLETED.getCode());
        stream.message.setTokenCount(stream.tokenCount);
        stream.message.setMetadata(JSON.toJSONString(Map.of(
            "runId", runId,
            "artifacts", artifactService.listViewsByRunId(runId),
            "sources", stream.sources,
            "dify", stream.metadata)));
        messageMapper.update(stream.message);
        rememberExchange(stream.conversation.getId(), stream.query, stream.message.getContent(), username);
        updateConversation(stream.conversation, username);
        eventService.publish(AgentV2EventType.RUN_COMPLETED, runId, Map.of(
            "messageId", stream.message.getId(), "tokenCount", stream.tokenCount));
    }

    private void fail(Long runId, V2StreamState stream, String code, String message, String username)
    {
        if (!runService.fail(runId, code, message, username))
        {
            return;
        }
        stream.message.setContent(stream.answer.toString());
        stream.message.setStatus(MessageStatus.FAILED.getCode());
        stream.message.setErrorMessage(message);
        stream.message.setMetadata(JSON.toJSONString(Map.of("runId", runId)));
        messageMapper.update(stream.message);
        updateConversation(stream.conversation, username);
        eventService.publish(AgentV2EventType.RUN_FAILED, runId, Map.of("errorCode", code, "message", message));
    }

    private void stopAssistantMessage(AgentRun run, String username, V2StreamState stream)
    {
        AgentMessage message = messageMapper.selectById(run.getAssistantMessageId());
        if (message != null && MessageStatus.STREAMING.getCode().equals(message.getStatus()))
        {
            if (stream != null) message.setContent(stream.answer.toString());
            message.setStatus(MessageStatus.STOPPED.getCode());
            message.setErrorMessage(null);
            message.setMetadata(JSON.toJSONString(Map.of("runId", run.getId())));
            messageMapper.update(message);
            AgentConversation conversation = conversationService.requireOwned(run.getConversationId(), run.getUserId());
            updateConversation(conversation, username);
        }
    }

    private void updateConversation(AgentConversation conversation, String username)
    {
        conversation.setMessageCount((conversation.getMessageCount() == null ? 0 : conversation.getMessageCount()) + 2);
        conversation.setUpdateBy(username);
        conversationMapper.update(conversation);
    }

    /** 保存短期对话摘要，Dify会话失效时仍能恢复最近业务上下文。 */
    private void rememberExchange(Long conversationId, String query, String answer, String username)
    {
        String previous = stateService.get(conversationId).getConversationSummary();
        String exchange = "用户：" + (query == null ? "" : query.trim()) + "\n助手："
            + (answer == null ? "" : answer.trim());
        String summary = (previous == null || previous.isBlank()) ? exchange : previous + "\n" + exchange;
        if (summary.length() > 4000)
        {
            summary = summary.substring(summary.length() - 4000);
        }
        stateService.rememberSummary(conversationId, summary, username);
    }

    private AgentMessage saveMessage(Long conversationId, MessageRole role, String content, MessageStatus status,
        String username)
    {
        AgentMessage message = new AgentMessage();
        message.setConversationId(conversationId);
        message.setRole(role.getCode());
        message.setContent(content);
        message.setStatus(status.getCode());
        message.setCreateBy(username);
        messageMapper.insert(message);
        return message;
    }

    private Map<String, Object> citationData(AgentSource source)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", source.getId());
        data.put("title", source.getTitle());
        data.put("content", source.getContent());
        data.put("score", source.getScore());
        data.put("documentName", source.getDocumentName());
        data.put("datasetName", source.getDatasetName());
        data.values().removeIf(java.util.Objects::isNull);
        return data;
    }

    private int totalTokens(Map<String, Object> metadata)
    {
        Object usageValue = metadata.get("usage");
        if (usageValue instanceof Map<?, ?> usage && usage.get("total_tokens") instanceof Number number)
        {
            return number.intValue();
        }
        return 0;
    }

    private DifyClientSettings settings()
    {
        return appConfigService.requireSettings(DifyAppCode.AGENT_SUPERVISOR.getCode());
    }

    private String difyUser(Long userId)
    {
        return "ruoyi-user-" + userId;
    }

    private String safeMessage(Exception error)
    {
        return error instanceof ServiceException && error.getMessage() != null
            ? error.getMessage() : "Agent服务执行失败，请稍后重试";
    }

    private static class V2StreamState
    {
        private final AgentConversation conversation;
        private final AgentMessage message;
        private final String query;
        private final StringBuilder answer = new StringBuilder();
        private final ReasoningContentFilter reasoningFilter = new ReasoningContentFilter();
        private final Map<String, Object> metadata = new LinkedHashMap<>();
        private final List<AgentSource> sources = new ArrayList<>();
        private final Set<String> sourceIds = new HashSet<>();
        private final Set<String> startedDifyToolSteps = new HashSet<>();
        private final Set<String> completedDifyToolSteps = new HashSet<>();
        private String taskId;
        private String workflowRunId;
        private int tokenCount;
        private volatile boolean cancelled;

        private V2StreamState(AgentConversation conversation, AgentMessage message, String query)
        {
            this.conversation = conversation;
            this.message = message;
            this.query = query;
        }
    }
}
