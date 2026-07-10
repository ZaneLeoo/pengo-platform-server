package com.ruoyi.agent.application.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.agent.domain.enums.AgentToolResultStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentToolResultTest
{
    @Test
    void shouldDefensivelyCopySuccessfulData()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("datasetId", 9L);

        AgentToolResult result = AgentToolResult.success("已查询数据", data);
        data.put("datasetId", 100L);

        assertEquals(AgentToolResultStatus.SUCCEEDED, result.getStatus());
        assertEquals(9L, result.getData().get("datasetId"));
        assertThrows(UnsupportedOperationException.class, () -> result.getData().put("unsafe", true));
    }

    @Test
    void shouldExposeControlledFailure()
    {
        AgentToolResult result = AgentToolResult.failure("DATA_QUERY_TIMEOUT", "查询超时", true);

        assertEquals(AgentToolResultStatus.FAILED, result.getStatus());
        assertEquals("DATA_QUERY_TIMEOUT", result.getError().code());
        assertTrue(result.getError().retryable());
    }
}
