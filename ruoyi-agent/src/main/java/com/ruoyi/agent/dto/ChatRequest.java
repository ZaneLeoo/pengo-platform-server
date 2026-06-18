package com.ruoyi.agent.dto;

import lombok.Data;

/**
 * 前端聊天请求
 *
 * @author Dylan
 */
@Data
public class ChatRequest
{
    /** 会话ID（null表示新会话） */
    private Long conversationId;

    /** 用户问题 */
    private String query;
}
