package com.ruoyi.agent.application.artifact;

import com.ruoyi.agent.domain.enums.AgentArtifactKind;
import com.ruoyi.agent.domain.enums.ChartType;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;

/** 校验 V2 产物白名单，禁止模型直接下发任意前端组件配置。 */
@Component
public class AgentV2ArtifactValidator
{
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_SERIES = 20;
    private static final int MAX_POINTS = 1000;
    private static final int MAX_COLUMNS = 50;
    private static final int MAX_ROWS = 1000;
    private static final int MAX_CODE_LENGTH = 200_000;

    /** 校验产物是否可以持久化并发送到前端。 */
    public boolean isValid(AgentV2Artifact artifact)
    {
        if (artifact == null || !AgentV2Artifact.VERSION.equals(artifact.getVersion())
            || !AgentArtifactKind.supports(artifact.getType()) || isBlank(artifact.getTitle())
            || artifact.getTitle().length() > MAX_TITLE_LENGTH)
        {
            return false;
        }
        AgentArtifactKind kind = AgentArtifactKind.valueOf(artifact.getType().toUpperCase());
        return switch (kind)
        {
            case CHART -> validChart(artifact.getPayload());
            case TABLE -> validTable(artifact.getPayload());
            case CODE -> validCode(artifact.getPayload());
            case FILE, DOCUMENT -> validFile(artifact);
            default -> true;
        };
    }

    /** 图表只接受语义数据，不接受任意 ECharts option。 */
    private boolean validChart(Map<String, Object> payload)
    {
        if (payload == null || payload.containsKey("option") || !ChartType.supports(text(payload.get("chartType"))))
        {
            return false;
        }
        Object seriesValue = payload.get("series");
        if (!(seriesValue instanceof List<?> series) || series.isEmpty() || series.size() > MAX_SERIES)
        {
            return false;
        }
        int pointCount = 0;
        for (Object value : series)
        {
            if (!(value instanceof Map<?, ?> item) || !(item.get("data") instanceof List<?> points))
            {
                return false;
            }
            pointCount += points.size();
        }
        Object categories = payload.get("categories");
        return pointCount <= MAX_POINTS && (!(categories instanceof List<?> values) || values.size() <= MAX_POINTS);
    }

    /** 表格限制列数和行数，具体单元格由前端按纯文本渲染。 */
    private boolean validTable(Map<String, Object> payload)
    {
        return payload != null && payload.get("columns") instanceof List<?> columns && !columns.isEmpty()
            && columns.size() <= MAX_COLUMNS && payload.get("rows") instanceof List<?> rows
            && rows.size() <= MAX_ROWS;
    }

    /** 代码仅作为文本产物，第一版不代表已经执行。 */
    private boolean validCode(Map<String, Object> payload)
    {
        return payload != null && payload.get("content") instanceof String content
            && !content.isBlank() && content.length() <= MAX_CODE_LENGTH
            && (!(payload.get("language") instanceof String language) || language.length() <= 32);
    }

    /** 文件必须有可信引用，且所有可点击地址只能是站内相对路径或HTTP(S)。 */
    private boolean validFile(AgentV2Artifact artifact)
    {
        boolean hasReference = artifact.getFileId() != null || !isBlank(artifact.getDownloadUrl());
        return hasReference && safeUrl(artifact.getPreviewUrl()) && safeUrl(artifact.getDownloadUrl());
    }

    private boolean safeUrl(String value)
    {
        if (isBlank(value)) return true;
        if (value.startsWith("/") && !value.startsWith("//")) return true;
        try
        {
            URI uri = new URI(value);
            return uri.isAbsolute() && ("https".equalsIgnoreCase(uri.getScheme())
                || "http".equalsIgnoreCase(uri.getScheme()));
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }

    private String text(Object value)
    {
        return value instanceof String text ? text : "";
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
