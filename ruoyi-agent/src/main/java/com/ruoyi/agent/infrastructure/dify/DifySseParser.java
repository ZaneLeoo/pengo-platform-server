package com.ruoyi.agent.infrastructure.dify;

import java.util.Optional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;

/**
 * 解析 Dify Chatflow 的 SSE data 行。
 */
@org.springframework.stereotype.Component
public class DifySseParser
{
    private static final String DATA_PREFIX = "data:";
    private static final String PING_LINE = "event: ping";

    /**
     * 解析单个 SSE 行。
     *
     * @param line SSE 原始行
     * @return 可识别的 Dify 事件；空行、ping 和非 data 行返回空
     */
    public Optional<DifyStreamEvent> parseDataLine(String line)
    {
        if (line == null || line.isBlank() || PING_LINE.equals(line.trim()))
        {
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith(DATA_PREFIX))
        {
            return Optional.empty();
        }

        String payload = trimmed.substring(DATA_PREFIX.length()).trim();
        try
        {
            return Optional.of(toEvent(JSON.parseObject(payload)));
        }
        catch (RuntimeException exception)
        {
            return Optional.of(parseError(exception));
        }
    }

    private DifyStreamEvent toEvent(JSONObject json)
    {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent(json.getString("event"));
        event.setTaskId(json.getString("task_id"));
        event.setMessageId(json.getString("message_id"));
        event.setConversationId(json.getString("conversation_id"));
        event.setWorkflowRunId(json.getString("workflow_run_id"));
        event.setAnswer(json.getString("answer"));
        event.setId(json.getString("id"));
        event.setPosition(json.getInteger("position"));
        event.setThought(json.getString("thought"));
        event.setObservation(json.getString("observation"));
        event.setTool(json.getString("tool"));
        event.setToolInput(json.getString("tool_input"));
        event.setToolLabels(json.getJSONObject("tool_labels"));
        event.setReason(json.getString("reason"));
        event.setStatus(json.getInteger("status"));
        event.setCode(json.getString("code"));
        event.setMessage(json.getString("message"));
        event.setData(json.getJSONObject("data"));
        event.setMetadata(json.getJSONObject("metadata"));
        event.setRaw(json);
        return event;
    }

    private DifyStreamEvent parseError(RuntimeException exception)
    {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent("error");
        event.setCode("parse_error");
        event.setMessage("无法解析 Dify 流式事件");
        return event;
    }
}
