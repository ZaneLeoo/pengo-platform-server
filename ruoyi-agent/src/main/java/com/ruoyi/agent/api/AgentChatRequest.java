package com.ruoyi.agent.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Agent 流式聊天请求。 */
@Data
public class AgentChatRequest {

    private Long conversationId;

    @NotBlank(message = "消息不能为空")
    private String query;

    private Map<String, Object> inputs = Collections.emptyMap();
}
