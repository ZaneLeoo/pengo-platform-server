package com.ruoyi.agent.infrastructure.dify.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dify Chatflow 消息请求。 */
@Data
@NoArgsConstructor
public class DifyChatRequest {
    private String query;
    private Map<String, Object> inputs;
    private String conversationId;
    private String user;

    public DifyChatRequest(String query, Map<String, Object> inputs, String conversationId, String user) {
        this.query = query;
        this.inputs = inputs == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
        this.conversationId = conversationId;
        this.user = user;
    }
}
