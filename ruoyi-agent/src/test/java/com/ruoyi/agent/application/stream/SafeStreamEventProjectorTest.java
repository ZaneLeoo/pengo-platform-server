package com.ruoyi.agent.application.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SafeStreamEventProjectorTest
{
    @Test
    void shouldExposeSafeNodeSummaryFromNestedData()
    {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent("node_started");
        event.setWorkflowRunId("run-1");
        event.setData(Map.of(
            "id", "node-execution-1",
            "node_id", "classifier-1",
            "title", "问题分类器",
            "node_type", "question-classifier",
            "status", "running",
            "index", 2,
            "elapsed_time", 0.18,
            "inputs", Map.of("query", "秘密问题"),
            "outputs", Map.of("category", "BI分析")));

        Map<String, Object> projected = new SafeStreamEventProjector().project(event);

        assertEquals("node_started", projected.get("event"));
        assertEquals("run-1", projected.get("workflowRunId"));
        assertEquals("classifier-1", projected.get("nodeId"));
        assertEquals("问题分类器", projected.get("title"));
        assertEquals("question-classifier", projected.get("nodeType"));
        assertEquals("running", projected.get("status"));
        assertEquals(2, projected.get("index"));
        assertEquals(0.18, projected.get("elapsedTime"));
        assertFalse(projected.containsKey("inputs"));
        assertFalse(projected.containsKey("outputs"));
    }

    @Test
    void shouldDropNestedObjectsAndOversizedTitles()
    {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent("node_finished");
        event.setData(Map.of(
            "title", "x".repeat(201),
            "status", Map.of("unsafe", true),
            "process_data", List.of("secret")));

        Map<String, Object> projected = new SafeStreamEventProjector().project(event);

        assertFalse(projected.containsKey("title"));
        assertFalse(projected.containsKey("status"));
        assertFalse(projected.containsKey("processData"));
    }
}
