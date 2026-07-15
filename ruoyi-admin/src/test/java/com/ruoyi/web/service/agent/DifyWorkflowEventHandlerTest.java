package com.ruoyi.web.service.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DifyWorkflowEventHandlerTest {
    private final DifyWorkflowEventHandler handler = new DifyWorkflowEventHandler();

    @Test
    void shouldHandleSupportedWorkflowLifecycleEvents() {
        assertTrue(handler.handle(event("workflow_started")));
        assertTrue(handler.handle(event("node_started")));
        assertTrue(handler.handle(event("node_finished")));
        assertTrue(handler.handle(event("workflow_finished")));
    }

    @Test
    void shouldLeaveExistingMessageEventsToCurrentHandlers() {
        assertFalse(handler.handle(event("message")));
        assertFalse(handler.handle(event("agent_thought")));
        assertFalse(handler.handle(event("message_end")));
        assertFalse(handler.handle(event("error")));
    }

    private DifyStreamEvent event(String eventType) {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent(eventType);
        event.setTaskId("task-1");
        event.setWorkflowRunId("workflow-1");
        event.setData(Map.of("node_id", "node-1", "title", "Agent", "status", "succeeded"));
        return event;
    }
}
