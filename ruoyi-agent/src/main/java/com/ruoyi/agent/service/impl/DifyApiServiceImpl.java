package com.ruoyi.agent.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.dto.DifyChatRequest;
import com.ruoyi.agent.dto.DifyEvent;
import com.ruoyi.agent.service.IDifyApiService;

/**
 * Dify API 调用服务实现
 *
 * @author Dylan
 */
@Service
public class DifyApiServiceImpl implements IDifyApiService
{
    private static final Logger log = LoggerFactory.getLogger(DifyApiServiceImpl.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public void streamChat(String apiBaseUrl, String apiKey, DifyChatRequest request,
                           AtomicBoolean cancelled, Consumer<DifyEvent> eventConsumer) throws IOException
    {
        String apiUrl = apiBaseUrl.trim();
        if (!apiUrl.endsWith("/")) {
            apiUrl += "/";
        }
        apiUrl += "v1/chat-messages";

        String body = JSON.toJSONString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        log.info("Dify request: POST {}", apiUrl);

        HttpResponse<java.io.InputStream> response;
        try
        {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Dify request interrupted", e);
        }

        int statusCode = response.statusCode();
        if (statusCode != 200) {
            // 读取错误响应体
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            log.error("Dify API returned {} : {}", statusCode, errorBody);
            DifyEvent errorEvent = new DifyEvent();
            errorEvent.setEvent("error");
            errorEvent.setMessage("Dify API error: HTTP " + statusCode + " - " + errorBody);
            eventConsumer.accept(errorEvent);
            return;
        }

        // 读取SSE流，逐行解析
        // 注意：Dify的SSE格式将event类型放在JSON内部（data: {"event": "message", ...}），
        // 而非标准SSE的 event: message 行。需要兼容两种格式。
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8)))
        {
            String line;
            String currentEvent = null;
            StringBuilder dataBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null)
            {
                if (cancelled.get())
                {
                    log.info("Dify stream cancelled by user");
                    break;
                }

                if (line.startsWith("event: "))
                {
                    // 标准SSE格式：event行指定事件类型
                    currentEvent = line.substring(7).trim();
                }
                else if (line.startsWith("data: "))
                {
                    dataBuilder.append(line.substring(6));
                }
                else if (line.isEmpty())
                {
                    // SSE事件边界（空行）
                    if (dataBuilder.length() == 0)
                    {
                        continue;
                    }

                    String data = dataBuilder.toString();
                    dataBuilder.setLength(0);

                    try
                    {
                        // Dify格式：event类型在JSON内部 {"event": "message", ...}
                        DifyEvent event = JSON.parseObject(data, DifyEvent.class);

                        // 如果标准SSE指定了event类型，优先使用
                        if (currentEvent != null && !currentEvent.isEmpty())
                        {
                            event.setEvent(currentEvent);
                        }
                        // 如果JSON内部也没有event字段，使用标准SSE的event类型
                        if (event.getEvent() == null || event.getEvent().isEmpty())
                        {
                            event.setEvent(currentEvent != null ? currentEvent : "message");
                        }

                        eventConsumer.accept(event);
                    }
                    catch (Exception e)
                    {
                        log.warn("Failed to parse Dify SSE data: {}", data, e);
                    }
                    currentEvent = null;
                }
            }
        }
    }

}
