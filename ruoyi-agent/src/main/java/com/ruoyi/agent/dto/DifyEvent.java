package com.ruoyi.agent.dto;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Dify SSE 事件解析
 *
 * @author Dylan
 */
@Data
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
}
