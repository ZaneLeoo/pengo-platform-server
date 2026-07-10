package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.application.artifact.AgentV2Artifact;
import com.ruoyi.agent.application.artifact.AgentV2ArtifactService;
import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.agent.domain.enums.ChartType;
import com.ruoyi.agent.domain.runtime.AgentArtifactRecord;
import com.ruoyi.agent.domain.runtime.AgentDataset;
import com.ruoyi.common.exception.ServiceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 将已有数据集转换为安全语义图表产物。 */
@Component
public class RenderChartTool implements AgentToolHandler
{
    private final DatasetToolSupport datasetSupport;
    private final AgentV2ArtifactService artifactService;

    public RenderChartTool(DatasetToolSupport datasetSupport, AgentV2ArtifactService artifactService)
    {
        this.datasetSupport = datasetSupport;
        this.artifactService = artifactService;
    }

    @Override
    public AgentToolCode code() { return AgentToolCode.RENDER_CHART; }

    @Override
    public AgentStepType stepType() { return AgentStepType.CHART_RENDER; }

    @Override
    public String displayName() { return "正在生成图表"; }

    /** 自动复用最近数据集，并允许后续轮次只改变图表类型。 */
    @Override
    public AgentToolResult execute(AgentToolContext context, Map<String, Object> input)
    {
        AgentDataset dataset = datasetSupport.requireDataset(context, input);
        List<Map<String, Object>> rows = datasetSupport.rows(dataset);
        if (rows.isEmpty())
        {
            throw new ServiceException("数据集为空，无法生成图表");
        }
        String chartType = chartType(input.get("chartType"));
        String categoryField = field(input.get("categoryField"), rows, false);
        String valueField = field(input.get("valueField"), rows, true);
        String title = text(input.get("title"));
        if (title.isBlank()) title = dataset.getDatasetName();

        List<Object> categories = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            categories.add(row.get(categoryField));
            values.add(datasetSupport.decimal(row.get(valueField), valueField).stripTrailingZeros());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chartType", chartType);
        payload.put("categories", categories);
        payload.put("series", List.of(Map.of("name", valueField, "data", values)));
        payload.put("categoryField", categoryField);
        payload.put("valueField", valueField);

        AgentV2Artifact artifact = new AgentV2Artifact();
        artifact.setType("CHART");
        artifact.setTitle(title);
        artifact.setDatasetId(dataset.getId());
        artifact.setPayload(payload);
        AgentArtifactRecord record = artifactService.create(context.conversationId(), context.runId(), null,
            artifact, context.username());
        artifact.setArtifactId(record.getId());

        AgentToolResult result = AgentToolResult.success("已生成" + chartTypeLabel(chartType), Map.of(
            "datasetId", dataset.getId(), "artifactId", record.getId(), "chartType", chartType));
        result.setArtifacts(List.of(artifact));
        result.setNextActions(List.of("更换图表类型", "继续分析数据"));
        return result;
    }

    private String chartType(Object value)
    {
        String type = text(value).toLowerCase(Locale.ROOT);
        if (type.isBlank()) type = ChartType.BAR.getCode();
        if (!ChartType.supports(type))
        {
            throw new ServiceException("仅支持bar、line、pie图表");
        }
        return type;
    }

    private String field(Object requested, List<Map<String, Object>> rows, boolean numeric)
    {
        String field = text(requested);
        if (!field.isBlank())
        {
            if (!datasetSupport.fields(rows).contains(field))
            {
                throw new ServiceException("数据集不存在字段：" + field);
            }
            if (numeric)
            {
                Object sample = rows.stream().map(row -> row.get(field)).filter(java.util.Objects::nonNull)
                    .findFirst().orElse(null);
                datasetSupport.decimal(sample, field);
            }
            return field;
        }
        for (String candidate : datasetSupport.fields(rows))
        {
            Object sample = rows.stream().map(row -> row.get(candidate)).filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
            if (numeric == (sample instanceof Number))
            {
                return candidate;
            }
        }
        throw new ServiceException(numeric ? "无法推断图表数值字段" : "无法推断图表分类字段");
    }

    private String chartTypeLabel(String type)
    {
        return switch (type)
        {
            case "line" -> "折线图";
            case "pie" -> "饼图";
            default -> "柱状图";
        };
    }

    private String text(Object value)
    {
        return value instanceof String text ? text.trim() : "";
    }
}
