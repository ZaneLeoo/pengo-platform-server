package com.ruoyi.agent.infrastructure.dify.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/** Dify Chatflow 消息请求。 */
@Data
@NoArgsConstructor
public class DifyChatRequest {
    private String query;
    private Map<String, Object> inputs;
    private String conversationId;
    private String user;
    private List<Map<String, Object>> files;

    public DifyChatRequest(String query, Map<String, Object> inputs, String conversationId, String user) {
        this(query, inputs, conversationId, user, Collections.emptyList());
    }

    public DifyChatRequest(String query, Map<String, Object> inputs, String conversationId, String user,
            List<Map<String, Object>> files) {
        this.query = query;
        this.inputs = inputs == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
        this.conversationId = conversationId;
        this.user = user;
        this.files = files == null ? Collections.emptyList() : List.copyOf(files);
    }
}
