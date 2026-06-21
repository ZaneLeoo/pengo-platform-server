package com.ruoyi.agent.application.artifact;

import com.ruoyi.agent.domain.enums.ArtifactType;
import com.ruoyi.agent.domain.enums.ChartType;
import java.util.List;
import java.util.Map;

/** 校验 Dify 输出的结构化产物是否满足安全协议。 */
@org.springframework.stereotype.Component
public class ArtifactValidator
{
    private static final String VERSION = "1.0";
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_SERIES = 20;
    private static final int MAX_POINTS = 1000;
    private static final int MAX_COLUMNS = 50;
    private static final int MAX_ROWS = 1000;

    /** 校验单个产物，校验失败时返回 false。 */
    public boolean isValid(Map<String, Object> artifact)
    {
        String type = text(artifact.get("type"));
        String title = text(artifact.get("title"));
        Object payloadValue = artifact.get("payload");
        if (!VERSION.equals(text(artifact.get("version"))) || !ArtifactType.supports(type)
            || title.isBlank() || title.length() > MAX_TITLE_LENGTH || !(payloadValue instanceof Map<?, ?> payload))
        {
            return false;
        }
        return ArtifactType.CHART.getCode().equals(type) ? validChart(payload) : validTable(payload);
    }

    /** 校验图表只包含前端可安全解释的白名单结构。 */
    private boolean validChart(Map<?, ?> payload)
    {
        if (!ChartType.supports(text(payload.get("chartType"))) || payload.containsKey("option"))
        {
            return false;
        }
        Object seriesValue = payload.get("series");
        if (!(seriesValue instanceof List<?> series) || series.isEmpty() || series.size() > MAX_SERIES)
        {
            return false;
        }
        int points = 0;
        for (Object item : series)
        {
            if (!(item instanceof Map<?, ?> seriesItem) || !(seriesItem.get("data") instanceof List<?> data))
            {
                return false;
            }
            points += data.size();
        }
        return points <= MAX_POINTS;
    }

    /** 校验表格列、行及单行宽度。 */
    private boolean validTable(Map<?, ?> payload)
    {
        if (!(payload.get("columns") instanceof List<?> columns) || columns.isEmpty()
            || columns.size() > MAX_COLUMNS || !(payload.get("rows") instanceof List<?> rows)
            || rows.size() > MAX_ROWS)
        {
            return false;
        }
        return rows.stream().allMatch(row -> row instanceof List<?> values && values.size() == columns.size());
    }

    /** 将可空对象转为协议文本。 */
    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
