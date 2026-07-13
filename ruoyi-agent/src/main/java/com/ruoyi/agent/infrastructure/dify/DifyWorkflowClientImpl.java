package com.ruoyi.agent.infrastructure.dify;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 基于 JDK HttpClient 的 Dify 工作流客户端。 */
@Component
public class DifyWorkflowClientImpl implements DifyWorkflowClient {
    private static final Logger log = LoggerFactory.getLogger(DifyWorkflowClientImpl.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(3);
    private static final String CRLF = "\r\n";

    private final HttpClient httpClient;

    public DifyWorkflowClientImpl() {
        this(HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
    }

    DifyWorkflowClientImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /** 上传本地文件，返回 Dify 文件 ID。 */
    @Override
    public DifyFileUploadResult uploadFile(DifyClientSettings settings, DifyFileUploadRequest request)
            throws IOException, InterruptedException {
        String boundary = "----RuoYiDifyBoundary" + UUID.randomUUID().toString().replace("-", "");
        HttpResponse<String> response = httpClient.send(uploadRequest(settings, request, boundary),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccess(response.statusCode(), response.body());
        JSONObject body = JSON.parseObject(response.body());
        return new DifyFileUploadResult(body.getString("id"), body.getString("name"), body.getLongValue("size"),
                body.getString("extension"), body.getString("mime_type"));
    }

    /** 以阻塞模式执行默认发布工作流。 */
    @Override
    public DifyWorkflowRunResult runBlocking(DifyClientSettings settings, DifyWorkflowRunRequest request)
            throws IOException, InterruptedException {
        return runWorkflow(settings, request, "blocking");
    }

    /** 以流式模式执行默认发布工作流，并返回 workflow_finished 的最终结果。 */
    @Override
    public DifyWorkflowRunResult runStreaming(DifyClientSettings settings, DifyWorkflowRunRequest request)
            throws IOException, InterruptedException {
        return runWorkflow(settings, request, "streaming");
    }

    private DifyWorkflowRunResult runWorkflow(DifyClientSettings settings, DifyWorkflowRunRequest request,
            String responseMode) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", request.inputs());
        body.put("response_mode", responseMode);
        body.put("user", request.user());
        String requestJson = JSON.toJSONString(body);
        log.debug("Dify workflow request: {}", requestJson);
        HttpResponse<String> response = httpClient.send(jsonRequest(settings, "/workflows/run", requestJson),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccess(response.statusCode(), response.body());
        log.debug("Dify workflow response: {}", response.body());
        return "streaming".equals(responseMode)
                ? parseStreamingResponse(response.body())
                : parseBlockingResponse(
                        response.body());
    }

    private DifyWorkflowRunResult parseBlockingResponse(String body) {
        JSONObject root = JSON.parseObject(body);
        JSONObject data = root.getJSONObject("data");
        Map<String, Object> outputs = data == null || data.get("outputs") == null
                ? new LinkedHashMap<>()
                : data.getJSONObject("outputs");
        String status = data == null ? null : data.getString("status");
        String error = data == null ? null : data.getString("error");
        return new DifyWorkflowRunResult(root.getString("task_id"), root.getString("workflow_run_id"), status,
                outputs, error, body);
    }

    private DifyWorkflowRunResult parseStreamingResponse(String body) throws IOException {
        DifyWorkflowRunResult finished = null;
        StringBuilder eventData = new StringBuilder();
        for (String line : body.split("\\R", -1)) {
            if (line.startsWith("data:")) {
                eventData.append(line.substring(5).trim());
            } else if (line.isBlank() && eventData.length() > 0) {
                finished = parseStreamingEvent(eventData.toString(), body, finished);
                eventData.setLength(0);
            }
        }
        if (eventData.length() > 0) {
            finished = parseStreamingEvent(eventData.toString(), body, finished);
        }
        if (finished == null) {
            throw new IOException("Dify 流式响应未返回 workflow_finished 事件");
        }
        return finished;
    }

    private DifyWorkflowRunResult parseStreamingEvent(String data, String rawResponse, DifyWorkflowRunResult current) {
        if (data.isBlank() || "[DONE]".equals(data)) {
            return current;
        }
        JSONObject root = JSON.parseObject(data);
        if (!"workflow_finished".equals(root.getString("event"))) {
            return current;
        }
        JSONObject eventData = root.getJSONObject("data");
        Map<String, Object> outputs = eventData == null || eventData.get("outputs") == null
                ? new LinkedHashMap<>()
                : eventData.getJSONObject("outputs");
        String status = eventData == null ? null : eventData.getString("status");
        String error = eventData == null ? null : eventData.getString("error");
        String workflowRunId = root.getString("workflow_run_id");
        if (workflowRunId == null && eventData != null) {
            workflowRunId = eventData.getString("id");
        }
        return new DifyWorkflowRunResult(root.getString("task_id"), workflowRunId, status, outputs, error,
                rawResponse);
    }

    private HttpRequest uploadRequest(DifyClientSettings settings, DifyFileUploadRequest request, String boundary) {
        return HttpRequest.newBuilder(uri(settings, "/files/upload")).timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + settings.apiKey())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArrays(multipartBody(request, boundary))).build();
    }

    private List<byte[]> multipartBody(DifyFileUploadRequest request, String boundary) {
        List<byte[]> parts = new ArrayList<>();
        addTextPart(parts, boundary, "user", request.user());
        addFilePart(parts, boundary, request);
        parts.add(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return parts;
    }

    private void addTextPart(List<byte[]> parts, String boundary, String name, String value) {
        parts.add(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"" + name + "\"" + CRLF + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        parts.add((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        parts.add(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    private void addFilePart(List<byte[]> parts, String boundary, DifyFileUploadRequest request) {
        parts.add(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + escape(request.filename()) + "\""
                + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Type: " + request.contentType() + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(request.content());
        parts.add(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    private HttpRequest jsonRequest(DifyClientSettings settings, String path, String body) {
        return HttpRequest.newBuilder(uri(settings, path)).timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + settings.apiKey()).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }

    private URI uri(DifyClientSettings settings, String path) {
        return URI.create(settings.baseUrl().replaceAll("/+$", "") + path);
    }

    private String escape(String value) {
        return value == null ? "file" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** 将非 2xx 响应转换为稳定的远程调用异常。 */
    private void requireSuccess(int statusCode, String body) throws IOException {
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Dify 请求失败，HTTP " + statusCode + "，" + body);
        }
    }
}
