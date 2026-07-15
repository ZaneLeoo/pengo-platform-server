package com.ruoyi.web.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DifyAgentLogAdapterTest {
    private final DifyAgentLogAdapter adapter = new DifyAgentLogAdapter();

    @Test
    void shouldAdaptToolStartLog() {
        DifyStreamEvent source = agentLog(Map.of(
                "id", "log-1",
                "label", "CALL queryMaterials",
                "status", "start",
                "data", Map.of()));

        DifyStreamEvent adapted = adapter.adapt(source).orElseThrow();

        assertEquals("agent_thought", adapted.getEvent());
        assertEquals("log-1", adapted.getId());
        assertEquals("queryMaterials", adapted.getTool());
        assertEquals(null, adapted.getObservation());
    }

    @Test
    void shouldAdaptCompletedChartLog() {
        Map<String, Object> output = Map.of(
                "tool_call_name", "bar_chart",
                "tool_call_input", Map.of("title", "库存统计", "data", "1;2"),
                "tool_response", "```echarts\n{\"series\":[]}\n```");
        DifyStreamEvent source = agentLog(Map.of(
                "id", "log-2",
                "label", "CALL bar_chart",
                "status", "success",
                "data", Map.of("output", output)));

        DifyStreamEvent adapted = adapter.adapt(source).orElseThrow();

        assertEquals("bar_chart", adapted.getTool());
        assertTrue(adapted.getToolInput().contains("库存统计"));
        assertTrue(adapted.getObservation().contains("echarts"));
    }

    @Test
    void shouldIgnoreAgentThoughtLogs() {
        DifyStreamEvent source = agentLog(Map.of(
                "id", "log-3",
                "label", "deepseek Thought",
                "status", "success",
                "data", Map.of("output", "thinking")));

        assertTrue(adapter.adapt(source).isEmpty());
    }

    private DifyStreamEvent agentLog(Map<String, Object> data) {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setEvent("agent_log");
        event.setTaskId("task-1");
        event.setWorkflowRunId("workflow-1");
        event.setData(data);
        return event;
    }
}
