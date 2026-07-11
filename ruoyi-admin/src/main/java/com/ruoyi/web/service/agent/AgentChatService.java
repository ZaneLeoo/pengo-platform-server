package com.ruoyi.web.service.agent;

import com.ruoyi.agent.api.AgentChatRequest;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.domain.enums.DifyAppCode;
import com.ruoyi.agent.infrastructure.dify.DifyChatflowClient;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 仅负责 RuoYi 与 Dify 间的基础流式聊天转发。 */
@Service
public class AgentChatService
{
    private static final long TIMEOUT = 10 * 60 * 1000L;
    private final DifyChatflowClient difyClient;
    private final DifyAppConfigService configService;
    private final Executor executor;

    public AgentChatService(DifyChatflowClient difyClient, DifyAppConfigService configService,
        @Qualifier("threadPoolTaskExecutor") Executor executor)
    {
        this.difyClient = difyClient;
        this.configService = configService;
        this.executor = executor;
    }

    /** 发起一次不附带工具运行上下文的基础聊天流。 */
    public SseEmitter stream(AgentChatRequest request, Long userId)
    {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        executor.execute(() -> forward(emitter, request, userId));
        return emitter;
    }

    private void forward(SseEmitter emitter, AgentChatRequest request, Long userId)
    {
        try
        {
            DifyClientSettings settings = configService.requireSettings(DifyAppCode.AGENT_SUPERVISOR.getCode());
            DifyChatRequest difyRequest = new DifyChatRequest(request.getQuery(), request.getInputs(),
                request.getDifyConversationId(), "ruoyi-user-" + userId);
            difyClient.stream(settings, difyRequest, event -> forwardEvent(emitter, event));
            send(emitter, "done", Map.of());
            emitter.complete();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            fail(emitter, "请求被中断");
        }
        catch (IOException | ServiceException e)
        {
            fail(emitter, safeMessage(e));
        }
        catch (RuntimeException e)
        {
            fail(emitter, "Dify 服务调用失败");
        }
    }

    private void forwardEvent(SseEmitter emitter, DifyStreamEvent event)
    {
        if ("agent_thought".equals(event.getEvent()))
        {
            forwardToolEvent(emitter, event);
            return;
        }
        if ("message".equals(event.getEvent()) || "agent_message".equals(event.getEvent()))
        {
            if (event.getAnswer() != null && !event.getAnswer().isEmpty())
            {
                send(emitter, "message", Map.of("content", event.getAnswer()));
            }
            return;
        }
        if ("message_end".equals(event.getEvent()))
        {
            Map<String, Object> data = new LinkedHashMap<>();
            if (event.getConversationId() != null) data.put("conversationId", event.getConversationId());
            if (event.getTaskId() != null) data.put("taskId", event.getTaskId());
            send(emitter, "metadata", data);
        }
        if ("error".equals(event.getEvent())) throw new ServiceException(safeMessage(event));
    }

    /** 将 Dify 工具执行状态转换为前端稳定的 tool 事件。 */
    private void forwardToolEvent(SseEmitter emitter, DifyStreamEvent event)
    {
        if (event.getTool() == null || event.getTool().isBlank())
        {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phase", event.getObservation() == null || event.getObservation().isBlank() ? "started" : "finished");
        data.put("callId", event.getId());
        data.put("toolName", event.getTool());
        data.put("toolLabel", resolveToolLabel(event));
        if (event.getPosition() != null) data.put("position", event.getPosition());
        if (event.getToolInput() != null && !event.getToolInput().isBlank())
        {
            data.put("input", parseStructuredValue(event.getToolInput()));
        }
        if (event.getObservation() != null && !event.getObservation().isBlank())
        {
            data.put("output", parseStructuredValue(event.getObservation()));
        }
        send(emitter, "tool", data);
    }

    /** 优先返回当前语言的工具展示名称。 */
    private String resolveToolLabel(DifyStreamEvent event)
    {
        Object label = event.getToolLabels().get(event.getTool());
        if (label instanceof Map<?, ?> labels)
        {
            Object zh = labels.get("zh_Hans");
            if (zh != null && !zh.toString().isBlank()) return zh.toString();
            Object en = labels.get("en_US");
            if (en != null && !en.toString().isBlank()) return en.toString();
        }
        return event.getTool();
    }

    /** 将工具输入和输出解析为 JSON；非 JSON 内容按字符串保留。 */
    private Object parseStructuredValue(String value)
    {
        try
        {
            return JSON.parse(value);
        }
        catch (RuntimeException ignored)
        {
            return value;
        }
    }

    private void fail(SseEmitter emitter, String message)
    {
        send(emitter, "error", Map.of("message", message));
        emitter.complete();
    }

    private void send(SseEmitter emitter, String name, Map<String, Object> data)
    {
        try { emitter.send(SseEmitter.event().name(name).data(data)); }
        catch (IOException ignored) { emitter.complete(); }
    }

    private String safeMessage(Exception error)
    {
        return error.getMessage() == null || error.getMessage().isBlank() ? "Dify 服务调用失败" : error.getMessage();
    }

    private String safeMessage(DifyStreamEvent event)
    {
        return event.getMessage() == null || event.getMessage().isBlank() ? "Dify 返回错误" : event.getMessage();
    }
}
