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
import org.springframework.stereotype.Component;

/** 基于 JDK HttpClient 的 Dify 工作流客户端。 */
@Component
public class DifyWorkflowClientImpl implements DifyWorkflowClient
{
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(3);
    private static final String CRLF = "\r\n";

    private final HttpClient httpClient;

    public DifyWorkflowClientImpl()
    {
        this(HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
    }

    DifyWorkflowClientImpl(HttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    /** 上传本地文件，返回 Dify 文件 ID。 */
    @Override
    public DifyFileUploadResult uploadFile(DifyClientSettings settings, DifyFileUploadRequest request)
        throws IOException, InterruptedException
    {
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
        throws IOException, InterruptedException
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", request.inputs());
        body.put("response_mode", "blocking");
        body.put("user", request.user());
        HttpResponse<String> response = httpClient.send(jsonRequest(settings, "/workflows/run", JSON.toJSONString(body)),
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccess(response.statusCode(), response.body());
        JSONObject root = JSON.parseObject(response.body());
        JSONObject data = root.getJSONObject("data");
        Map<String, Object> outputs = data == null || data.get("outputs") == null
            ? new LinkedHashMap<>() : data.getJSONObject("outputs");
        String status = data == null ? null : data.getString("status");
        String error = data == null ? null : data.getString("error");
        return new DifyWorkflowRunResult(root.getString("task_id"), root.getString("workflow_run_id"), status,
            outputs, error, response.body());
    }

    private HttpRequest uploadRequest(DifyClientSettings settings, DifyFileUploadRequest request, String boundary)
    {
        return HttpRequest.newBuilder(uri(settings, "/files/upload")).timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + settings.apiKey())
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArrays(multipartBody(request, boundary))).build();
    }

    private List<byte[]> multipartBody(DifyFileUploadRequest request, String boundary)
    {
        List<byte[]> parts = new ArrayList<>();
        addTextPart(parts, boundary, "user", request.user());
        addFilePart(parts, boundary, request);
        parts.add(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return parts;
    }

    private void addTextPart(List<byte[]> parts, String boundary, String name, String value)
    {
        parts.add(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"" + name + "\"" + CRLF + CRLF)
            .getBytes(StandardCharsets.UTF_8));
        parts.add((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        parts.add(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    private void addFilePart(List<byte[]> parts, String boundary, DifyFileUploadRequest request)
    {
        parts.add(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + escape(request.filename()) + "\""
            + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Type: " + request.contentType() + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        parts.add(request.content());
        parts.add(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    private HttpRequest jsonRequest(DifyClientSettings settings, String path, String body)
    {
        return HttpRequest.newBuilder(uri(settings, path)).timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + settings.apiKey()).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }

    private URI uri(DifyClientSettings settings, String path)
    {
        return URI.create(settings.baseUrl().replaceAll("/+$", "") + path);
    }

    private String escape(String value)
    {
        return value == null ? "file" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** 将非 2xx 响应转换为稳定的远程调用异常。 */
    private void requireSuccess(int statusCode, String body) throws IOException
    {
        if (statusCode < 200 || statusCode >= 300)
        {
            throw new IOException("Dify 请求失败，HTTP " + statusCode + "，" + body);
        }
    }
}
