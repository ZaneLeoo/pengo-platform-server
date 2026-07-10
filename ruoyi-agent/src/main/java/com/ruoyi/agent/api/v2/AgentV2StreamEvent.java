package com.ruoyi.agent.api.v2;

import com.ruoyi.agent.domain.enums.AgentV2EventType;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Spring 对前端发布的版本化 Agent V2 事件。 */
public record AgentV2StreamEvent(String event, Long runId, Long sequence, long timestamp, Map<String, Object> data)
{
    /** 创建一个不可变的标准事件。 */
    public static AgentV2StreamEvent of(AgentV2EventType type, Long runId, Long sequence, Map<String, Object> data)
    {
        Map<String, Object> safeData = data == null ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(data));
        return new AgentV2StreamEvent(type.getCode(), runId, sequence, Instant.now().toEpochMilli(), safeData);
    }
}
