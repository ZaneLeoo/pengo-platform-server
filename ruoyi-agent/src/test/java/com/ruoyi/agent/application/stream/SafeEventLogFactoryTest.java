package com.ruoyi.agent.application.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SafeEventLogFactoryTest
{
    @Test
    void shouldKeepIdentifiersButDropContentAndRawPayload()
    {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent("message");
        event.setTaskId("task-1");
        event.setMessageId("message-1");
        event.setAnswer("<think>secret</think>answer");
        event.setData(Map.of("thought", "secret"));
        event.setRaw(Map.of("answer", "secret", "inputs", Map.of("password", "value")));

        Map<String, Object> safe = new SafeEventLogFactory().create(event);

        assertEquals("message", safe.get("event"));
        assertEquals("task-1", safe.get("taskId"));
        assertEquals("message-1", safe.get("messageId"));
        assertFalse(safe.containsKey("answer"));
        assertFalse(safe.containsKey("data"));
        assertFalse(safe.containsKey("raw"));
    }
}
