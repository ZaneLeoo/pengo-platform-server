package com.ruoyi.agent.infrastructure.dify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DifyChatflowClientImplTest {
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
    void shouldSendStreamingRequestAndConsumeEvents() throws Exception {
        server.createContext("/chat-messages", exchange -> {
            assertEquals("Bearer secret", exchange.getRequestHeaders().getFirst("Authorization"));
            Map<String, Object> body = JSON
                    .parseObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("streaming", body.get("response_mode"));
            assertEquals(Boolean.FALSE, body.get("auto_generate_name"));
            List<Map<String, Object>> files = (List<Map<String, Object>>) body.get("files");
            assertEquals("image", files.get(0).get("type"));
            assertEquals("local_file", files.get(0).get("transfer_method"));
            assertEquals("file-1", files.get(0).get("upload_file_id"));
            byte[] response = ("data: {\"event\":\"message\",\"answer\":\"你好\",\"task_id\":\"task-1\"}\n\n"
                    + "event: ping\n\n").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        List<DifyStreamEvent> events = new ArrayList<>();

        new DifyChatflowClientImpl().stream(new DifyClientSettings(baseUrl, "secret"),
                new DifyChatRequest("你好", Map.of("scene", "sales"), null, "ruoyi-user-1",
                        List.of(Map.of("type", "image", "transfer_method", "local_file", "upload_file_id", "file-1"))),
                events::add);

        assertEquals(1, events.size());
        assertEquals("你好", events.get(0).getAnswer());
        assertEquals("task-1", events.get(0).getTaskId());
    }

    @Test
    void shouldRecordEveryRawSseLineBeforeParsing() throws Exception {
        server.createContext("/chat-messages", exchange -> {
            byte[] response = ("data: {\"event\":\"message\",\"answer\":\"你好\"}\n\n"
                    + "event: ping\n\n").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        List<String> rawLines = new ArrayList<>();
        DifyRawEventLogger rawEventLogger = new DifyRawEventLogger(rawLines::add, () -> true);
        DifyChatflowClientImpl client = new DifyChatflowClientImpl(
                java.net.http.HttpClient.newHttpClient(), new DifySseParser(), rawEventLogger);
        List<DifyStreamEvent> events = new ArrayList<>();

        client.stream(new DifyClientSettings(baseUrl, "secret"),
                new DifyChatRequest("你好", Map.of(), null, "ruoyi-user-1"), events::add);

        assertEquals(List.of("data: {\"event\":\"message\",\"answer\":\"你好\"}", "event: ping"), rawLines);
        assertEquals(1, events.size());
    }

    @Test
    void shouldCallStopEndpoint() throws Exception {
        server.createContext("/chat-messages/task-1/stop", exchange -> {
            assertTrue(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
                    .contains("ruoyi-user-1"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        new DifyChatflowClientImpl().stop(new DifyClientSettings(baseUrl, "secret"), "task-1", "ruoyi-user-1");
    }
}
