package com.ruoyi.web.service.agent;

import com.ruoyi.agent.api.AgentChatRequest;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.domain.enums.DifyAppCode;
import com.ruoyi.agent.domain.enums.AgentStreamEventType;
import com.ruoyi.agent.infrastructure.dify.DifyChatflowClient;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final KnowledgeBaseService knowledgeBaseService;
    private final Executor executor;

    public AgentChatService(DifyChatflowClient difyClient, DifyAppConfigService configService,
        KnowledgeBaseService knowledgeBaseService,
        @Qualifier("threadPoolTaskExecutor") Executor executor)
    {
        this.difyClient = difyClient;
        this.configService = configService;
        this.knowledgeBaseService = knowledgeBaseService;
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
            send(emitter, AgentStreamEventType.DONE, Map.of());
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
                send(emitter, AgentStreamEventType.MESSAGE, Map.of("content", event.getAnswer()));
            }
            return;
        }
        if ("message_end".equals(event.getEvent()))
        {
            Map<String, Object> data = new LinkedHashMap<>();
            if (event.getConversationId() != null) data.put("conversationId", event.getConversationId());
            if (event.getTaskId() != null) data.put("taskId", event.getTaskId());
            send(emitter, AgentStreamEventType.METADATA, data);
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
        if (isChartTool(event.getTool()))
        {
            forwardChartEvents(emitter, event);
            return;
        }
        boolean knowledgeEvent = event.getTool().startsWith("dataset_");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phase", event.getObservation() == null || event.getObservation().isBlank() ? "started" : "finished");
        data.put("callId", event.getId());
        data.put(knowledgeEvent ? "datasetId" : "toolName", event.getTool());
        data.put(knowledgeEvent ? "datasetLabel" : "toolLabel",
            knowledgeEvent ? knowledgeBaseService.resolveName(event.getTool()) : resolveToolLabel(event));
        if (event.getPosition() != null) data.put("position", event.getPosition());
        if (event.getToolInput() != null && !event.getToolInput().isBlank())
        {
            Object input = parseStructuredValue(event.getToolInput());
            if (knowledgeEvent)
            {
                String query = extractKnowledgeQuery(input, event.getTool());
                data.put("query", query);
                if (event.getObservation() != null && !event.getObservation().isBlank())
                {
                    data.put("sources", knowledgeBaseService.retrieveSources(event.getTool(), query));
                }
            }
            else
            {
                data.put("input", input);
            }
        }
        if (event.getObservation() != null && !event.getObservation().isBlank())
        {
            data.put("output", parseStructuredValue(event.getObservation()));
        }
        send(emitter, knowledgeEvent ? AgentStreamEventType.KNOWLEDGE : AgentStreamEventType.TOOL, data);
    }

    /** 将一个 Dify 并行图表工具事件拆分为多个标准 chart 事件。 */
    private void forwardChartEvents(SseEmitter emitter, DifyStreamEvent event)
    {
        JSONObject inputs = parseObject(event.getToolInput());
        JSONObject outputs = parseObject(event.getObservation());
        for (String toolName : event.getTool().split(";"))
        {
            String chartType = chartType(toolName);
            if (chartType == null) continue;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("phase", outputs == null ? "started" : "finished");
            data.put("callId", event.getId() + "-" + toolName);
            data.put("toolName", toolName);
            data.put("chartType", chartType);
            if (event.getPosition() != null) data.put("position", event.getPosition());
            JSONObject toolInput = nestedObject(inputs, toolName);
            if (toolInput != null && toolInput.getString("title") != null)
            {
                data.put("title", toolInput.getString("title"));
            }
            if (outputs != null)
            {
                String option = outputs.getString(toolName);
                JSONObject optionJson = extractEchartsOption(option);
                if (optionJson != null) data.put("option", optionJson);
            }
            send(emitter, AgentStreamEventType.CHART, data);
        }
    }

    /** 判断是否包含受支持的图表工具。 */
    private boolean isChartTool(String toolNames)
    {
        for (String toolName : toolNames.split(";"))
        {
            if (chartType(toolName) != null) return true;
        }
        return false;
    }

    /** 将插件工具名映射为受支持的图表类型。 */
    private String chartType(String toolName)
    {
        return switch (toolName)
        {
            case "bar_chart" -> "bar";
            case "line_chart" -> "line";
            case "pie_chart" -> "pie";
            default -> null;
        };
    }

    /** 解析 Dify 工具输入或输出对象。 */
    private JSONObject parseObject(String value)
    {
        if (value == null || value.isBlank()) return null;
        try
        {
            return JSON.parseObject(value);
        }
        catch (RuntimeException ignored)
        {
            return null;
        }
    }

    /** 读取并行工具对象中的单个工具参数。 */
    private JSONObject nestedObject(JSONObject object, String key)
    {
        if (object == null) return null;
        Object value = object.get(key);
        if (value instanceof JSONObject json) return json;
        if (value instanceof String text) return parseObject(text);
        return null;
    }

    /** 从 ```echarts 代码块中提取并解析图表 option。 */
    private JSONObject extractEchartsOption(String output)
    {
        if (output == null || output.isBlank()) return null;
        String normalized = output.trim();
        int start = normalized.indexOf("```echarts");
        if (start >= 0)
        {
            normalized = normalized.substring(start + "```echarts".length());
            int end = normalized.indexOf("```");
            if (end >= 0) normalized = normalized.substring(0, end);
        }
        return parseObject(normalized.trim());
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

    /** 提取 Dify 知识库工具输入中的查询文本。 */
    private String extractKnowledgeQuery(Object input, String datasetId)
    {
        if (input instanceof Map<?, ?> values)
        {
            Object datasetInput = values.get(datasetId);
            if (datasetInput instanceof Map<?, ?> queryValues && queryValues.get("query") != null)
            {
                return queryValues.get("query").toString();
            }
            if (datasetInput != null) return datasetInput.toString();
        }
        return input == null ? "" : input.toString();
    }

    private void fail(SseEmitter emitter, String message)
    {
        send(emitter, AgentStreamEventType.ERROR, Map.of("message", message));
        emitter.complete();
    }

    private void send(SseEmitter emitter, String name, Map<String, Object> data)
    {
        try { emitter.send(SseEmitter.event().name(name).data(data)); }
        catch (IOException ignored) { emitter.complete(); }
    }

    /** 使用统一事件枚举发送 SSE，避免业务代码散落硬编码事件名称。 */
    private void send(SseEmitter emitter, AgentStreamEventType type, Map<String, Object> data)
    {
        send(emitter, type.getValue(), data);
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
