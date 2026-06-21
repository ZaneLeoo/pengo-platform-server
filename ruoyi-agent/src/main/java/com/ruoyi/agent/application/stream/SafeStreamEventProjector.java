package com.ruoyi.agent.application.stream;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 将 Dify 流程事件投影为前端可展示的安全摘要。 */
@Component
public class SafeStreamEventProjector
{
    private static final int MAX_TEXT_LENGTH = 200;

    /**
     * 提取节点展示所需的白名单字段，不透传输入、输出和过程数据。
     *
     * @param event Dify 原始解析事件
     * @return 前端流程展示事件
     */
    public Map<String, Object> project(DifyStreamEvent event)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        putText(result, "event", event.getEvent());
        putText(result, "workflowRunId", event.getWorkflowRunId());

        Map<String, Object> data = event.getData();
        putText(result, "nodeId", first(data.get("node_id"), data.get("id")));
        putText(result, "title", data.get("title"));
        putText(result, "nodeType", data.get("node_type"));
        putScalar(result, "status", first(data.get("status"), event.getStatus()));
        putScalar(result, "index", data.get("index"));
        putScalar(result, "elapsedTime", data.get("elapsed_time"));
        return result;
    }

    /** 返回第一个非空候选值。 */
    private Object first(Object primary, Object fallback)
    {
        return primary == null ? fallback : primary;
    }

    /** 仅写入长度受限的文本值。 */
    private void putText(Map<String, Object> target, String key, Object value)
    {
        if (value instanceof String text && !text.isBlank() && text.length() <= MAX_TEXT_LENGTH)
        {
            target.put(key, text);
        }
    }

    /** 仅写入协议允许的简单标量，拒绝任意嵌套对象。 */
    private void putScalar(Map<String, Object> target, String key, Object value)
    {
        if (value instanceof Number || value instanceof Boolean)
        {
            target.put(key, value);
        }
        else
        {
            putText(target, key, value);
        }
    }
}
