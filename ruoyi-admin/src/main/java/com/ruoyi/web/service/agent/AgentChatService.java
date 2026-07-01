package com.ruoyi.web.service.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.api.AgentChatRequest;
import com.ruoyi.agent.application.AgentConversationService;
import com.ruoyi.agent.application.AgentStreamRegistry;
import com.ruoyi.agent.application.artifact.AgentArtifact;
import com.ruoyi.agent.application.artifact.ArtifactExtractor;
import com.ruoyi.agent.application.source.AgentSource;
import com.ruoyi.agent.application.source.SourceExtractor;
import com.ruoyi.agent.application.stream.ReasoningContentFilter;
import com.ruoyi.agent.application.stream.SafeEventLogFactory;
import com.ruoyi.agent.application.stream.SafeStreamEventProjector;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.domain.enums.AgentConfigKey;
import com.ruoyi.agent.domain.enums.DifyEventType;
import com.ruoyi.agent.domain.enums.MessageRole;
import com.ruoyi.agent.domain.enums.MessageStatus;
import com.ruoyi.agent.domain.enums.StreamEventType;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 编排一次本地消息持久化、Dify 流和前端 SSE 输出。 */
@Service
public class AgentChatService
{
    private static final long SSE_TIMEOUT = 10 * 60 * 1000L;
    private final AgentConversationService conversationService;
    private final AgentConversationMapper conversationMapper;
    private final AgentMessageMapper messageMapper;
    private final DifyChatflowClient difyClient;
    private final ISysConfigService configService;
    private final AgentStreamRegistry registry;
    private final Executor executor;
    private final ArtifactExtractor artifactExtractor;
    private final SourceExtractor sourceExtractor;
    private final SafeEventLogFactory safeEventLogFactory;
    private final SafeStreamEventProjector streamEventProjector;

    public AgentChatService(AgentConversationService conversationService, AgentConversationMapper conversationMapper,
        AgentMessageMapper messageMapper, DifyChatflowClient difyClient, ISysConfigService configService,
        AgentStreamRegistry registry, ArtifactExtractor artifactExtractor, SourceExtractor sourceExtractor,
        SafeEventLogFactory safeEventLogFactory, SafeStreamEventProjector streamEventProjector,
        @Qualifier("threadPoolTaskExecutor") Executor executor)
    {
        this.conversationService = conversationService;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.difyClient = difyClient;
        this.configService = configService;
        this.registry = registry;
        this.artifactExtractor = artifactExtractor;
        this.sourceExtractor = sourceExtractor;
        this.safeEventLogFactory = safeEventLogFactory;
        this.streamEventProjector = streamEventProjector;
        this.executor = executor;
    }

    /** 创建 SSE 连接，并在业务线程中执行完整聊天流程。 */
    public SseEmitter stream(AgentChatRequest request, Long userId, String username)
    {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        executor.execute(() -> executeStream(emitter, request, userId, username));
        return emitter;
    }

    /** 校验会话归属并调用 Dify 停止当前 task。 */
    public void stop(Long conversationId, Long userId, String username)
    {
        conversationService.requireOwned(conversationId, userId);
        String taskId = registry.find(conversationId);
        if (taskId == null)
        {
            throw new ServiceException("当前会话没有正在生成的任务");
        }
        registry.requestStop(conversationId);
        try { difyClient.stop(settings(), taskId, difyUser(userId)); }
        catch (InterruptedException e) { registry.clearStopRequest(conversationId); Thread.currentThread().interrupt(); throw new ServiceException("停止生成被中断"); }
        catch (IOException e) { registry.clearStopRequest(conversationId); throw new ServiceException(e.getMessage()); }
    }

    /** 执行远程流、事件转发和最终落库。 */
    private void executeStream(SseEmitter emitter, AgentChatRequest request, Long userId, String username)
    {
        AgentConversation conversation = request.getConversationId() == null
            ? conversationService.create(userId, request.getQuery(), username)
            : conversationService.requireOwned(request.getConversationId(), userId);
        saveUserMessage(conversation.getId(), request.getQuery(), username);
        AgentMessage assistant = createAssistantMessage(conversation.getId(), username);
        StreamState state = new StreamState(conversation, assistant);
        try
        {
            send(emitter, StreamEventType.CONVERSATION, Map.of("conversationId", conversation.getId(), "title", conversation.getTitle()));
            DifyChatRequest difyRequest = new DifyChatRequest(request.getQuery(), request.getInputs(),
                conversation.getDifyConversationId(), difyUser(userId));
            difyClient.stream(settings(), difyRequest, event -> handleEvent(emitter, state, event));
            forwardVisibleText(emitter, state, state.reasoningFilter.finish());
            complete(state, username, registry.isStopRequested(conversation.getId())
                ? MessageStatus.STOPPED : MessageStatus.COMPLETED);
            send(emitter, StreamEventType.DONE, donePayload(state));
            emitter.complete();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            fail(emitter, state, "请求被中断");
        }
        catch (Exception e)
        {
            if (registry.isStopRequested(conversation.getId()))
            {
                complete(state, username, MessageStatus.STOPPED);
                send(emitter, StreamEventType.DONE, donePayload(state));
                emitter.complete();
            }
            else
            {
                fail(emitter, state, e.getMessage() == null ? "Dify 服务调用失败" : e.getMessage());
            }
        }
        finally { registry.remove(conversation.getId()); }
    }

    /** 将 Dify 事件转换成稳定的前端事件。 */
    private void handleEvent(SseEmitter emitter, StreamState state, DifyStreamEvent event)
    {
        state.events.add(safeEventLogFactory.create(event));
        updateIdentifiers(state, event);
        DifyEventType type = DifyEventType.fromCode(event.getEvent());
        if (type == DifyEventType.MESSAGE || type == DifyEventType.AGENT_MESSAGE)
        {
            forwardVisibleText(emitter, state, state.reasoningFilter.accept(event.getAnswer()));
        }
        else if (type == DifyEventType.MESSAGE_REPLACE)
        {
            ReasoningContentFilter replacementFilter = new ReasoningContentFilter();
            String replacement = replacementFilter.accept(event.getAnswer()) + replacementFilter.finish();
            state.answer.setLength(0);
            state.answer.append(replacement);
            send(emitter, StreamEventType.MESSAGE_REPLACE, Map.of("content", state.answer.toString()));
        }
        else if (type == DifyEventType.NODE_FINISHED || type == DifyEventType.WORKFLOW_FINISHED)
        {
            if (type == DifyEventType.WORKFLOW_FINISHED && "failed".equals(String.valueOf(event.getData().get("status"))))
            {
                Object error = event.getData().get("error");
                throw new ServiceException(error == null ? "Dify 工作流执行失败" : String.valueOf(error));
            }
            send(emitter, type == DifyEventType.NODE_FINISHED ? StreamEventType.NODE : StreamEventType.WORKFLOW, publicEvent(event));
            for (AgentArtifact artifact : artifactExtractor.extract(event.getData()))
            {
                state.artifacts.add(artifact);
                send(emitter, StreamEventType.ARTIFACT, artifact);
            }
            List<AgentSource> sources = sourceExtractor.extractFromNodeData(event.getData());
            if (addSources(state, sources))
            {
                send(emitter, StreamEventType.SOURCES, Map.of("sources", sources));
            }
        }
        else if (type.isWorkflowEvent())
        {
            send(emitter, StreamEventType.WORKFLOW, publicEvent(event));
        }
        else if (type.isNodeEvent())
        {
            send(emitter, StreamEventType.NODE, publicEvent(event));
        }
        else if (type == DifyEventType.ERROR)
        {
            throw new ServiceException(event.getMessage() == null ? "Dify 返回错误" : event.getMessage());
        }
        else if (type == DifyEventType.MESSAGE_END)
        {
            state.difyMetadata.putAll(event.getMetadata());
            List<AgentSource> sources = sourceExtractor.extractFromMetadata(event.getMetadata());
            if (addSources(state, sources))
            {
                send(emitter, StreamEventType.SOURCES, Map.of("sources", sources));
            }
            state.message.setTokenCount(totalTokens(event.getMetadata()));
        }
    }

    /** 将非空安全正文同时写入消息缓冲并发送给前端。 */
    private void forwardVisibleText(SseEmitter emitter, StreamState state, String text)
    {
        if (text == null || text.isEmpty())
        {
            return;
        }
        state.answer.append(text);
        send(emitter, StreamEventType.MESSAGE, Map.of("content", text));
    }

    /** 合并知识来源，避免同一 Dify 资源在节点和 message_end 中重复落库。 */
    private boolean addSources(StreamState state, List<AgentSource> sources)
    {
        boolean changed = false;
        for (AgentSource source : sources)
        {
            boolean exists = state.sources.stream().anyMatch(item -> Objects.equals(item.getId(), source.getId()));
            if (!exists)
            {
                state.sources.add(source);
                changed = true;
            }
        }
        return changed;
    }

    /** 更新 Dify 标识并登记可停止的 task。 */
    private void updateIdentifiers(StreamState state, DifyStreamEvent event)
    {
        if (event.getTaskId() != null) { state.message.setTaskId(event.getTaskId()); state.conversation.setLastTaskId(event.getTaskId()); registry.register(state.conversation.getId(), event.getTaskId()); }
        if (event.getMessageId() != null) state.message.setDifyMessageId(event.getMessageId());
        if (event.getWorkflowRunId() != null) { state.message.setWorkflowRunId(event.getWorkflowRunId()); state.conversation.setLastWorkflowRunId(event.getWorkflowRunId()); }
        if (event.getConversationId() != null) state.conversation.setDifyConversationId(event.getConversationId());
    }

    /** 保存用户消息。 */
    private void saveUserMessage(Long conversationId, String content, String username)
    {
        AgentMessage message = new AgentMessage(); message.setConversationId(conversationId);
        message.setRole(MessageRole.USER.getCode()); message.setContent(content);
        message.setStatus(MessageStatus.COMPLETED.getCode()); message.setCreateBy(username); messageMapper.insert(message);
    }

    /** 创建等待增量写入的助手消息。 */
    private AgentMessage createAssistantMessage(Long conversationId, String username)
    {
        AgentMessage message = new AgentMessage(); message.setConversationId(conversationId);
        message.setRole(MessageRole.ASSISTANT.getCode()); message.setContent("");
        message.setStatus(MessageStatus.STREAMING.getCode()); message.setCreateBy(username); messageMapper.insert(message); return message;
    }

    /** 成功结束时持久化消息正文、事件和会话标识。 */
    private void complete(StreamState state, String username, MessageStatus status)
    {
        state.message.setContent(state.answer.toString()); state.message.setEventLog(JSON.toJSONString(state.events));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("artifacts", state.artifacts); metadata.put("sources", state.sources); metadata.put("dify", state.difyMetadata);
        state.message.setMetadata(JSON.toJSONString(metadata));
        state.message.setStatus(status.getCode()); messageMapper.update(state.message);
        state.conversation.setMessageCount((state.conversation.getMessageCount() == null ? 0 : state.conversation.getMessageCount()) + 2);
        state.conversation.setUpdateBy(username); conversationMapper.update(state.conversation);
    }

    /** 失败时保存可读错误并结束 SSE。 */
    private void fail(SseEmitter emitter, StreamState state, String message)
    {
        state.message.setContent(state.answer.toString()); state.message.setStatus(MessageStatus.FAILED.getCode());
        state.message.setErrorMessage(message); state.message.setEventLog(JSON.toJSONString(state.events)); messageMapper.update(state.message);
        send(emitter, StreamEventType.ERROR, Map.of("message", message)); emitter.complete();
    }

    /** 只向前端公开展示所需的流程信息。 */
    private Map<String, Object> publicEvent(DifyStreamEvent event)
    {
        return streamEventProjector.project(event);
    }

    /** 构建完成事件。 */
    private Map<String, Object> donePayload(StreamState state)
    {
        Map<String, Object> value = new LinkedHashMap<>(); value.put("messageId", state.message.getId());
        value.put("taskId", state.message.getTaskId()); value.put("workflowRunId", state.message.getWorkflowRunId());
        value.put("tokenCount", state.message.getTokenCount()); value.put("metadata", state.difyMetadata); return value;
    }

    /** 从 Dify message_end metadata.usage 中读取总 token 数。 */
    private int totalTokens(Map<String, Object> metadata)
    {
        Object usageValue = metadata.get("usage");
        if (usageValue instanceof Map<?, ?> usage && usage.get("total_tokens") instanceof Number number)
        {
            return number.intValue();
        }
        return 0;
    }

    /** 安全发送单个 SSE 事件。 */
    private void send(SseEmitter emitter, StreamEventType type, Object data)
    {
        try { emitter.send(SseEmitter.event().name(type.getCode()).data(data)); }
        catch (IOException e) { throw new ServiceException("客户端连接已断开"); }
    }

    /** 从系统参数读取 Dify 设置并检查密钥。 */
    private DifyClientSettings settings()
    {
        String baseUrl = configService.selectConfigByKey(AgentConfigKey.DIFY_API_BASE_URL.getKey());
        String apiKey = configService.selectConfigByKey(AgentConfigKey.DIFY_API_KEY.getKey());
        if (apiKey == null || apiKey.isBlank()) throw new ServiceException("请先配置 Dify API Key");
        return new DifyClientSettings(baseUrl == null || baseUrl.isBlank() ? "https://api.dify.ai/v1" : baseUrl, apiKey);
    }

    /** 为 Dify 构造稳定且不泄露用户名的终端用户标识。 */
    private String difyUser(Long userId) { return "ruoyi-user-" + userId; }

    private static class StreamState
    {
        private final AgentConversation conversation; private final AgentMessage message;
        private final StringBuilder answer = new StringBuilder(); private final List<Map<String, Object>> events = new ArrayList<>();
        private final List<AgentArtifact> artifacts = new ArrayList<>();
        private final List<AgentSource> sources = new ArrayList<>();
        private final Map<String, Object> difyMetadata = new LinkedHashMap<>();
        private final ReasoningContentFilter reasoningFilter = new ReasoningContentFilter();
        private StreamState(AgentConversation conversation, AgentMessage message) { this.conversation = conversation; this.message = message; }
    }
}
