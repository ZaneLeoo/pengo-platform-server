package com.ruoyi.agent.infrastructure.dify;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/** 基于 JDK HttpClient 的 Dify Chatflow 流式客户端。 */
@Component
public class DifyChatflowClientImpl implements DifyChatflowClient {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(10);
    private final HttpClient httpClient;
    private final DifySseParser parser;
    private final DifyRawEventLogger rawEventLogger;

    public DifyChatflowClientImpl() {
        this(HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build(), new DifySseParser(),
                new DifyRawEventLogger());
    }

    DifyChatflowClientImpl(HttpClient httpClient, DifySseParser parser) {
        this(httpClient, parser, new DifyRawEventLogger());
    }

    /** 创建具有可替换基础设施依赖的 Dify 客户端。 */
    DifyChatflowClientImpl(HttpClient httpClient, DifySseParser parser, DifyRawEventLogger rawEventLogger) {
        this.httpClient = httpClient;
        this.parser = parser;
        this.rawEventLogger = rawEventLogger;
    }

    /** 发送流式消息，并逐行解析 Dify SSE。 */
    @Override
    public void stream(DifyClientSettings settings, DifyChatRequest request, Consumer<DifyStreamEvent> consumer)
            throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", request.query());
        body.put("inputs", request.inputs());
        body.put("response_mode", "streaming");
        body.put("auto_generate_name", false);
        body.put("user", request.user());
        if (request.conversationId() != null && !request.conversationId().isBlank()) {
            body.put("conversation_id", request.conversationId());
        }
        HttpResponse<java.io.InputStream> response = httpClient.send(build(settings,
                "/chat-messages", JSON.toJSONString(body)), HttpResponse.BodyHandlers.ofInputStream());
        requireSuccess(response.statusCode());
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rawEventLogger.log(line);
                parser.parseDataLine(line).ifPresent(consumer);
            }
        }
    }

    /** 调用 Dify 停止生成接口。 */
    @Override
    public void stop(DifyClientSettings settings, String taskId, String user) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(build(settings, "/chat-messages/" + taskId + "/stop",
                JSON.toJSONString(Map.of("user", user))), HttpResponse.BodyHandlers.ofString());
        requireSuccess(response.statusCode());
    }

    /** 构建统一鉴权和超时策略的 POST 请求。 */
    private HttpRequest build(DifyClientSettings settings, String path, String body) {
        String baseUrl = settings.baseUrl().replaceAll("/+$", "");
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + settings.apiKey()).header("Content-Type", "application/json")
                .header("Accept", "text/event-stream").POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }

    /** 将非 2xx 响应转换为稳定的远程调用异常。 */
    private void requireSuccess(int statusCode) throws IOException {
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Dify 请求失败，HTTP " + statusCode);
        }
    }
}
