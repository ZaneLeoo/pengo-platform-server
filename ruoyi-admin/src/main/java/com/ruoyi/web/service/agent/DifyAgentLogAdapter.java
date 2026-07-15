package com.ruoyi.web.service.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 将 Chatflow Agent 节点的 agent_log 工具记录适配为现有工具事件模型。 */
@Component
public class DifyAgentLogAdapter {
    private static final String AGENT_LOG_EVENT = "agent_log";
    private static final String CALL_LABEL_PREFIX = "CALL ";

    /**
     * 将工具调用日志转换为兼容 agent_thought 的事件。
     *
     * @param source Chatflow 原始事件
     * @return 工具开始或完成事件；非工具日志返回空
     */
    public Optional<DifyStreamEvent> adapt(DifyStreamEvent source) {
        if (!AGENT_LOG_EVENT.equals(source.getEvent())) {
            return Optional.empty();
        }

        Map<String, Object> log = source.getData();
        String label = stringValue(log.get("label"));
        if (label == null || !label.startsWith(CALL_LABEL_PREFIX)) {
            return Optional.empty();
        }

        Map<?, ?> output = nestedMap(log.get("data"), "output");
        String toolName = output == null ? null : stringValue(output.get("tool_call_name"));
        if (toolName == null || toolName.isBlank()) {
            toolName = label.substring(CALL_LABEL_PREFIX.length()).trim();
        }
        if (toolName.isBlank()) {
            return Optional.empty();
        }

        DifyStreamEvent target = new DifyStreamEvent();
        target.setEvent("agent_thought");
        target.setTaskId(source.getTaskId());
        target.setMessageId(source.getMessageId());
        target.setConversationId(source.getConversationId());
        target.setWorkflowRunId(source.getWorkflowRunId());
        target.setId(stringValue(log.get("id")));
        target.setTool(toolName);
        if (output != null) {
            target.setToolInput(jsonValue(output.get("tool_call_input")));
            target.setObservation(textValue(output.get("tool_response")));
        }
        return Optional.of(target);
    }

    private Map<?, ?> nestedMap(Object parent, String key) {
        if (!(parent instanceof Map<?, ?> values)) {
            return null;
        }
        Object nested = values.get(key);
        return nested instanceof Map<?, ?> nestedValues ? nestedValues : null;
    }

    private String jsonValue(Object value) {
        return value == null ? null : JSON.toJSONString(value);
    }

    private String textValue(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof String text ? text : JSON.toJSONString(value);
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
