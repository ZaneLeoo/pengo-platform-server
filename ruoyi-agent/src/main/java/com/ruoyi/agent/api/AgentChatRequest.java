package com.ruoyi.agent.api;

import jakarta.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Agent 流式聊天请求。 */
public class AgentChatRequest
{
    private Long conversationId;
    @NotBlank(message = "消息不能为空")
    private String query;
    private Map<String, Object> inputs = Collections.emptyMap();
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long value) { this.conversationId = value; }
    public String getQuery() { return query; }
    public void setQuery(String value) { this.query = value; }
    public Map<String, Object> getInputs() { return inputs; }
    public void setInputs(Map<String, Object> value) { this.inputs = value == null ? Collections.emptyMap() : new LinkedHashMap<>(value); }
}
