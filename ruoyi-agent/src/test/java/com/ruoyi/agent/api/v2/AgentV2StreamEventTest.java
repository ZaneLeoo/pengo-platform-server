package com.ruoyi.agent.api.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ruoyi.agent.domain.enums.AgentV2EventType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentV2StreamEventTest
{
    @Test
    void shouldCreateStableImmutableEvent()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("displayName", "正在查询业务数据");

        AgentV2StreamEvent event = AgentV2StreamEvent.of(AgentV2EventType.STEP_STARTED, 10L, 3L, data);
        data.put("displayName", "被修改");

        assertEquals("step.started", event.event());
        assertEquals(10L, event.runId());
        assertEquals(3L, event.sequence());
        assertEquals("正在查询业务数据", event.data().get("displayName"));
        assertThrows(UnsupportedOperationException.class, () -> event.data().put("unsafe", true));
    }
}
