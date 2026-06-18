package com.ruoyi.agent.dto;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 发送给 Dify /v1/chat-messages 的请求体
 *
 * @author Dylan
 */
public class DifyChatRequest
{
    /** 输入变量 */
    private Map<String, Object> inputs = new HashMap<>();

    /** 用户问题 */
    private String query;

    /** 响应模式：streaming（流式）或 blocking（阻塞） */
    @JsonProperty("response_mode")
    private String responseMode = "streaming";

    /** Dify侧会话ID（null表示新会话） */
    @JsonProperty("conversation_id")
    private String conversationId;

    /** 用户标识（RuoYi用户ID） */
    private String user;

    public Map<String, Object> getInputs()
    {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs)
    {
        this.inputs = inputs;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getResponseMode()
    {
        return responseMode;
    }

    public void setResponseMode(String responseMode)
    {
        this.responseMode = responseMode;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }
}
