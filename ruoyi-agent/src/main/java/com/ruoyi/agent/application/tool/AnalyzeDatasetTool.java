package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.agent.domain.runtime.AgentDataset;
import com.ruoyi.common.exception.ServiceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 对已有数据集执行确定性的基础统计，不让模型自行计算数值。 */
@Component
public class AnalyzeDatasetTool implements AgentToolHandler
{
    private final DatasetToolSupport datasetSupport;

    public AnalyzeDatasetTool(DatasetToolSupport datasetSupport)
    {
        this.datasetSupport = datasetSupport;
    }

    @Override
    public AgentToolCode code() { return AgentToolCode.ANALYZE_DATASET; }

    @Override
    public AgentStepType stepType() { return AgentStepType.DATA_ANALYSIS; }

    @Override
    public String displayName() { return "正在分析数据"; }

    /** 返回指定数值字段的合计、平均、最小和最大值。 */
    @Override
    public AgentToolResult execute(AgentToolContext context, Map<String, Object> input)
    {
        AgentDataset dataset = datasetSupport.requireDataset(context, input);
        List<Map<String, Object>> rows = datasetSupport.rows(dataset);
        if (rows.isEmpty())
        {
            return AgentToolResult.success("数据集为空，没有可分析的数据",
                Map.of("datasetId", dataset.getId(), "rowCount", 0));
        }
        String valueField = text(input.get("valueField"));
        if (valueField.isBlank())
        {
            valueField = inferNumericField(rows);
        }
        final String resolvedField = valueField;
        List<BigDecimal> values = rows.stream().map(row -> datasetSupport.decimal(row.get(resolvedField), resolvedField))
            .toList();
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP)
            .stripTrailingZeros();
        BigDecimal minimum = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maximum = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("datasetId", dataset.getId());
        statistics.put("valueField", resolvedField);
        statistics.put("rowCount", rows.size());
        statistics.put("sum", sum.stripTrailingZeros());
        statistics.put("average", average);
        statistics.put("minimum", minimum.stripTrailingZeros());
        statistics.put("maximum", maximum.stripTrailingZeros());
        AgentToolResult result = AgentToolResult.success("已完成" + resolvedField + "字段统计分析", statistics);
        result.setNextActions(List.of("生成图表"));
        return result;
    }

    private String inferNumericField(List<Map<String, Object>> rows)
    {
        for (String field : datasetSupport.fields(rows))
        {
            Object value = rows.stream().map(row -> row.get(field)).filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
            if (value instanceof Number)
            {
                return field;
            }
        }
        throw new ServiceException("数据集中没有可分析的数值字段，请指定valueField");
    }

    private String text(Object value)
    {
        return value instanceof String text ? text.trim() : "";
    }
}
