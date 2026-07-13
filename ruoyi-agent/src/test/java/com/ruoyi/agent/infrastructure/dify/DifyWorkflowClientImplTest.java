package com.ruoyi.agent.infrastructure.dify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DifyWorkflowClientImplTest {
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void shouldUploadFileWithMultipartForm() throws Exception {
        server.createContext("/files/upload", exchange -> {
            assertEquals("Bearer secret", exchange.getRequestHeaders().getFirst("Authorization"));
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            assertTrue(contentType.startsWith("multipart/form-data; boundary="));
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(body.contains("name=\"user\""));
            assertTrue(body.contains("ruoyi-user-1"));
            assertTrue(body.contains("filename=\"bom.png\""));
            assertTrue(body.contains("abc"));
            byte[] response = "{\"id\":\"file-1\",\"name\":\"bom.png\",\"size\":3,\"extension\":\"png\",\"mime_type\":\"image/png\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        DifyFileUploadResult result = new DifyWorkflowClientImpl().uploadFile(new DifyClientSettings(baseUrl, "secret"),
                new DifyFileUploadRequest("bom.png", "image/png", "abc".getBytes(StandardCharsets.UTF_8),
                        "ruoyi-user-1"));

        assertEquals("file-1", result.getId());
        assertEquals("png", result.getExtension());
    }

    @Test
    void shouldRunBlockingWorkflow() throws Exception {
        server.createContext("/workflows/run", exchange -> {
            assertEquals("Bearer secret", exchange.getRequestHeaders().getFirst("Authorization"));
            Map<String, Object> body = JSON
                    .parseObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("blocking", body.get("response_mode"));
            assertEquals("ruoyi-user-1", body.get("user"));
            byte[] response = ("{\"task_id\":\"task-1\",\"workflow_run_id\":\"run-1\",\"data\":{\"status\":\"succeeded\","
                    + "\"outputs\":{\"result\":\"{\\\"version\\\":\\\"1.0\\\"}\"}}}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        DifyWorkflowRunResult result = new DifyWorkflowClientImpl().runBlocking(
                new DifyClientSettings(baseUrl, "secret"),
                new DifyWorkflowRunRequest(Map.of("query", "识别BOM"), "ruoyi-user-1"));

        assertEquals("task-1", result.getTaskId());
        assertEquals("run-1", result.getWorkflowRunId());
        assertEquals("succeeded", result.getStatus());
        assertTrue(String.valueOf(result.getOutputs().get("result")).contains("version"));
    }

    @Test
    void shouldRunStreamingWorkflowAndUseFinishedOutputs() throws Exception {
        server.createContext("/workflows/run", exchange -> {
            assertEquals("Bearer secret", exchange.getRequestHeaders().getFirst("Authorization"));
            Map<String, Object> body = JSON.parseObject(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8));
            assertEquals("streaming", body.get("response_mode"));
            String stream = ""
                    + "data: {\"event\":\"workflow_started\",\"task_id\":\"task-2\",\"workflow_run_id\":\"run-2\"}\n\n"
                    + "data: {\"event\":\"node_finished\",\"task_id\":\"task-2\",\"workflow_run_id\":\"run-2\"}\n\n"
                    + "data: {\"event\":\"workflow_finished\",\"task_id\":\"task-2\",\"workflow_run_id\":\"run-2\","
                    + "\"data\":{\"id\":\"run-2\",\"status\":\"succeeded\",\"outputs\":{\"result\":\"{\\\"version\\\":\\\"1.0\\\"}\"}}}\n\n";
            byte[] response = stream.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        DifyWorkflowRunResult result = new DifyWorkflowClientImpl().runStreaming(
                new DifyClientSettings(baseUrl, "secret"), new DifyWorkflowRunRequest(Map.of("query", "识别BOM"),
                        "ruoyi-user-1"));

        assertEquals("task-2", result.getTaskId());
        assertEquals("run-2", result.getWorkflowRunId());
        assertEquals("succeeded", result.getStatus());
        assertTrue(String.valueOf(result.getOutputs().get("result")).contains("version"));
    }
}
