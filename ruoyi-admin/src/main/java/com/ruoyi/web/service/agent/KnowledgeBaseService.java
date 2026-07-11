package com.ruoyi.web.service.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/** Dify 知识库名称解析服务。 */
@Service
public class KnowledgeBaseService
{
    private static final String API_BASE_URL_KEY = "agent.dify.knowledge.api_base_url";
    private static final String API_KEY_KEY = "agent.dify.knowledge.api_key";
    private static final String CACHE_KEY_PREFIX = "agent:knowledge:dataset:";
    private static final int CACHE_TTL_HOURS = 24;
    private static final String DEFAULT_LABEL = "知识库";

    private final ISysConfigService configService;
    private final RedisCache redisCache;
    private final HttpClient httpClient;

    public KnowledgeBaseService(ISysConfigService configService, RedisCache redisCache)
    {
        this.configService = configService;
        this.redisCache = redisCache;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    /** 根据 Dify Agent 工具名称解析知识库显示名称。 */
    public String resolveName(String toolName)
    {
        String datasetId = normalizeDatasetId(toolName);
        if (StringUtils.isBlank(datasetId))
        {
            return DEFAULT_LABEL;
        }

        String cacheKey = CACHE_KEY_PREFIX + datasetId;
        String cachedName = redisCache.getCacheObject(cacheKey);
        if (StringUtils.isNotBlank(cachedName))
        {
            return cachedName;
        }

        String name = requestName(datasetId);
        if (StringUtils.isBlank(name))
        {
            return DEFAULT_LABEL;
        }
        redisCache.setCacheObject(cacheKey, name, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return name;
    }

    /** 根据用户查询检索知识库分段，返回前端可展示的来源摘要。 */
    public List<Map<String, Object>> retrieveSources(String toolName, String query)
    {
        String datasetId = normalizeDatasetId(toolName);
        String baseUrl = configService.selectConfigByKey(API_BASE_URL_KEY);
        String apiKey = configService.selectConfigByKey(API_KEY_KEY);
        if (StringUtils.isBlank(datasetId) || StringUtils.isBlank(query)
            || StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey))
        {
            return Collections.emptyList();
        }
        try
        {
            String body = JSON.toJSONString(Map.of("query", query));
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl.replaceAll("/+$", "") + "/datasets/" + datasetId + "/retrieve"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                return Collections.emptyList();
            }
            JSONObject result = JSON.parseObject(response.body());
            return toSources(result);
        }
        catch (Exception ignored)
        {
            return Collections.emptyList();
        }
    }

    /** 将 Agent 工具名 dataset_xxx 转换为知识库 API 使用的 UUID。 */
    private String normalizeDatasetId(String toolName)
    {
        if (StringUtils.isBlank(toolName) || !toolName.startsWith("dataset_"))
        {
            return StringUtils.EMPTY;
        }
        String candidate = toolName.substring("dataset_".length()).replace('_', '-');
        try
        {
            return UUID.fromString(candidate).toString();
        }
        catch (IllegalArgumentException ignored)
        {
            return StringUtils.EMPTY;
        }
    }

    /** 调用 Dify 知识库详情 API。 */
    private String requestName(String datasetId)
    {
        String baseUrl = configService.selectConfigByKey(API_BASE_URL_KEY);
        String apiKey = configService.selectConfigByKey(API_KEY_KEY);
        if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey))
        {
            return StringUtils.EMPTY;
        }
        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl.replaceAll("/+$", "") + "/datasets/" + datasetId))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                return StringUtils.EMPTY;
            }
            JSONObject body = JSON.parseObject(response.body());
            return body == null ? StringUtils.EMPTY : body.getString("name");
        }
        catch (Exception ignored)
        {
            return StringUtils.EMPTY;
        }
    }

    /** 将 Dify 分段响应转换为稳定的来源字段。 */
    private List<Map<String, Object>> toSources(JSONObject result)
    {
        if (result == null || result.getJSONArray("records") == null)
        {
            return Collections.emptyList();
        }
        List<Map<String, Object>> sources = new ArrayList<>();
        result.getJSONArray("records").stream().limit(5).forEach(item -> {
            if (!(item instanceof JSONObject record)) return;
            JSONObject segment = record.getJSONObject("segment");
            if (segment == null) return;
            JSONObject document = segment.getJSONObject("document");
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("sourceId", segment.getString("id"));
            source.put("segmentId", segment.getString("id"));
            source.put("documentId", segment.getString("document_id"));
            source.put("documentName", document == null ? "文档片段" : document.getString("name"));
            source.put("content", segment.getString("content"));
            source.put("score", record.getBigDecimal("score"));
            sources.add(source);
        });
        return sources;
    }
}
