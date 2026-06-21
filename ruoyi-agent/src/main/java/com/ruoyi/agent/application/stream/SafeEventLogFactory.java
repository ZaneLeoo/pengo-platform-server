package com.ruoyi.agent.application.stream;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 创建不包含消息正文、推理内容和工作流输入输出的安全事件快照。 */
@Component
public class SafeEventLogFactory
{
    /**
     * 创建可持久化的事件标识快照。
     *
     * @param event Dify 原始解析事件
     * @return 仅含事件类型、状态和追踪标识的快照
     */
    public Map<String, Object> create(DifyStreamEvent event)
    {
        Map<String, Object> safe = new LinkedHashMap<>();
        safe.put("event", event.getEvent());
        safe.put("taskId", event.getTaskId());
        safe.put("messageId", event.getMessageId());
        safe.put("conversationId", event.getConversationId());
        safe.put("workflowRunId", event.getWorkflowRunId());
        safe.put("status", event.getStatus());
        safe.put("code", event.getCode());
        return safe;
    }
}
