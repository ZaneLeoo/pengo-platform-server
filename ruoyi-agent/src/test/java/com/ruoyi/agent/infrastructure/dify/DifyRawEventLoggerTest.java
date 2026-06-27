package com.ruoyi.agent.infrastructure.dify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DifyRawEventLoggerTest
{
    @Test
    void shouldLogRawEventWhenDebugIsEnabled()
    {
        List<String> logs = new ArrayList<>();
        DifyRawEventLogger logger = new DifyRawEventLogger(logs::add, () -> true);

        logger.log("data: {\"event\":\"node_started\",\"task_id\":\"task-1\"}");

        assertEquals(List.of("data: {\"event\":\"node_started\",\"task_id\":\"task-1\"}"), logs);
    }

    @Test
    void shouldIgnoreBlankLinesAndDisabledDebug()
    {
        List<String> logs = new ArrayList<>();
        DifyRawEventLogger enabledLogger = new DifyRawEventLogger(logs::add, () -> true);
        DifyRawEventLogger disabledLogger = new DifyRawEventLogger(logs::add, () -> false);

        enabledLogger.log("  ");
        disabledLogger.log("event: ping");

        assertTrue(logs.isEmpty());
    }

    @Test
    void shouldMaskSecretsAndLargeBase64Content()
    {
        List<String> logs = new ArrayList<>();
        DifyRawEventLogger logger = new DifyRawEventLogger(logs::add, () -> true);
        String base64 = "A".repeat(300);

        logger.log("data: {\"api_key\":\"app-secret-value\",\"token\":\"token-value\",\"audio\":\"" + base64 + "\"}");

        String logged = logs.get(0);
        assertFalse(logged.contains("app-secret-value"));
        assertFalse(logged.contains("token-value"));
        assertFalse(logged.contains(base64));
        assertTrue(logged.contains("***"));
        assertTrue(logged.contains("[BASE64_REDACTED]"));
    }

    @Test
    void shouldTruncateEventLongerThanLimit()
    {
        List<String> logs = new ArrayList<>();
        DifyRawEventLogger logger = new DifyRawEventLogger(logs::add, () -> true);

        logger.log("data: " + "x".repeat(40_000));

        assertTrue(logs.get(0).length() <= DifyRawEventLogger.MAX_LOG_LENGTH);
        assertTrue(logs.get(0).endsWith("...[TRUNCATED]"));
    }
}
