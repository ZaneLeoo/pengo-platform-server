package com.ruoyi.agent.dto;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dify SSE 事件解析
 *
 * @author Dylan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyEvent
{
    /** 事件类型：message / agent_message / agent_thought / message_end / error */
    private String event;

    /** Dify侧会话ID（message_end事件返回） */
    @JsonProperty("conversation_id")
    private String conversationId;

    /** Dify侧消息ID（message_end事件返回） */
    @JsonProperty("message_id")
    private String messageId;

    /** 增量文本内容（message事件） */
    private String answer;

    /** 思考内容（agent_message事件） */
    private String thought;

    /** 观察/工具返回结果（agent_thought事件） */
    private String observation;

    /** 工具名称（agent_thought事件） */
    private String tool;

    /** 工具输入参数JSON（agent_thought事件） */
    @JsonProperty("tool_input")
    private String toolInput;

    /** 工具标签（agent_thought事件） */
    @JsonProperty("tool_label")
    private String toolLabel;

    /** 消息元数据（message_end事件） */
    private Map<String, Object> metadata;

    /** Token用量（message_end事件） */
    @JsonProperty("total_tokens")
    private Integer totalTokens;

    /** 错误状态码（error事件） */
    private String status;

    /** 错误码（error事件） */
    private String code;

    /** 错误消息（error事件） */
    private String message;

    // ---- Getters & Setters ----

    public String getEvent()
    {
        return event;
    }

    public void setEvent(String event)
    {
        this.event = event;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getThought()
    {
        return thought;
    }

    public void setThought(String thought)
    {
        this.thought = thought;
    }

    public String getObservation()
    {
        return observation;
    }

    public void setObservation(String observation)
    {
        this.observation = observation;
    }

    public String getTool()
    {
        return tool;
    }

    public void setTool(String tool)
    {
        this.tool = tool;
    }

    public String getToolInput()
    {
        return toolInput;
    }

    public void setToolInput(String toolInput)
    {
        this.toolInput = toolInput;
    }

    public String getToolLabel()
    {
        return toolLabel;
    }

    public void setToolLabel(String toolLabel)
    {
        this.toolLabel = toolLabel;
    }

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = metadata;
    }

    public Integer getTotalTokens()
    {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens)
    {
        this.totalTokens = totalTokens;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
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
}
