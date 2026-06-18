package com.ruoyi.agent.dto;

/**
 * 前端聊天请求
 *
 * @author ruoyi
 */
public class ChatRequest
{
    /** 会话ID（null表示新会话） */
    private Long conversationId;

    /** 用户问题 */
    private String query;

    public Long getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(Long conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }
}
