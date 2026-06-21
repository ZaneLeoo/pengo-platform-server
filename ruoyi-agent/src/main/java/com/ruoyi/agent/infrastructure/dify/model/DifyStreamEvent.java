package com.ruoyi.agent.infrastructure.dify.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dify Chatflow 流式事件。
 */
public class DifyStreamEvent
{
    private String event;
    private String taskId;
    private String messageId;
    private String conversationId;
    private String workflowRunId;
    private String answer;
    private String reason;
    private Integer status;
    private String code;
    private String message;
    private Map<String, Object> data = Collections.emptyMap();
    private Map<String, Object> metadata = Collections.emptyMap();
    private Map<String, Object> raw = Collections.emptyMap();

    public String getEvent()
    {
        return event;
    }

    public void setEvent(String event)
    {
        this.event = event;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getWorkflowRunId()
    {
        return workflowRunId;
    }

    public void setWorkflowRunId(String workflowRunId)
    {
        this.workflowRunId = workflowRunId;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public void setData(Map<String, Object> data)
    {
        this.data = immutableCopy(data);
    }

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = immutableCopy(metadata);
    }

    public Map<String, Object> getRaw()
    {
        return raw;
    }

    public void setRaw(Map<String, Object> raw)
    {
        this.raw = immutableCopy(raw);
    }

    private Map<String, Object> immutableCopy(Map<String, Object> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
