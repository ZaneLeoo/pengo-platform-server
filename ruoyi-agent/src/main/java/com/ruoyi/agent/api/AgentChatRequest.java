package com.ruoyi.agent.api;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;

/** 基础 Dify 聊天请求。 */
@Data
public class AgentChatRequest {
    @NotBlank(message = "消息不能为空")
    private String query;

    private String difyConversationId;

    private Map<String, Object> inputs;
}
