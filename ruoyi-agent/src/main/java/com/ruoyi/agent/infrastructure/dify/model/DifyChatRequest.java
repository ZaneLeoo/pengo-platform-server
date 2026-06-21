package com.ruoyi.agent.infrastructure.dify.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dify Chatflow 消息请求。 */
public record DifyChatRequest(String query, Map<String, Object> inputs, String conversationId, String user)
{
    public DifyChatRequest
    {
        inputs = inputs == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
    }
}
