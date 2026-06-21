package com.ruoyi.agent.infrastructure.dify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;

class DifySseParserTest
{
    private final DifySseParser parser = new DifySseParser();

    @Test
    void shouldParseMessageEvent()
    {
        String line = "data: {\"event\":\"message\",\"task_id\":\"task-1\","
                + "\"message_id\":\"message-1\",\"conversation_id\":\"conversation-1\","
                + "\"answer\":\"你好\"}";

        DifyStreamEvent event = parser.parseDataLine(line).orElseThrow();

        assertEquals("message", event.getEvent());
        assertEquals("task-1", event.getTaskId());
        assertEquals("message-1", event.getMessageId());
        assertEquals("conversation-1", event.getConversationId());
        assertEquals("你好", event.getAnswer());
    }

    @Test
    void shouldPreserveWorkflowData()
    {
        String line = "data: {\"event\":\"node_finished\",\"workflow_run_id\":\"run-1\","
                + "\"data\":{\"title\":\"问题分类器\",\"status\":\"succeeded\","
                + "\"outputs\":{\"category\":\"BI分析\"}}}";

        DifyStreamEvent event = parser.parseDataLine(line).orElseThrow();

        assertEquals("run-1", event.getWorkflowRunId());
        assertEquals("问题分类器", event.getData().get("title"));
        assertTrue(event.getRaw().containsKey("data"));
    }

    @Test
    void shouldIgnorePingAndBlankLines()
    {
        assertTrue(parser.parseDataLine("event: ping").isEmpty());
        assertTrue(parser.parseDataLine(" ").isEmpty());
    }

    @Test
    void shouldReturnParseErrorInsteadOfThrowing()
    {
        Optional<DifyStreamEvent> parsed = parser.parseDataLine("data: {invalid-json}");

        assertTrue(parsed.isPresent());
        assertEquals("error", parsed.get().getEvent());
        assertEquals("parse_error", parsed.get().getCode());
    }
}
