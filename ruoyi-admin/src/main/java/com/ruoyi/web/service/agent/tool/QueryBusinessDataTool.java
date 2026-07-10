package com.ruoyi.web.service.agent.tool;

import com.ruoyi.agent.application.dataset.AgentDatasetService;
import com.ruoyi.agent.application.memory.AgentSessionStateService;
import com.ruoyi.agent.application.tool.AgentToolContext;
import com.ruoyi.agent.application.tool.AgentToolHandler;
import com.ruoyi.agent.application.tool.AgentToolResult;
import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.agent.domain.runtime.AgentDataset;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsQuery;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsRow;
import com.ruoyi.mes.analytics.enums.MaterialStatisticsDimension;
import com.ruoyi.mes.analytics.service.MesAnalyticsService;
import com.ruoyi.mes.common.enums.MaterialType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 查询白名单业务数据并保存为可复用 Dataset。 */
@Component
public class QueryBusinessDataTool implements AgentToolHandler
{
    private static final String MATERIAL_DOMAIN = "MATERIAL";
    private static final String COUNT_OPERATION = "COUNT";
    private static final String MATERIAL_LIST_PERMISSION = "mes:material:list";

    private final AgentToolPermissionService permissionService;
    private final MesAnalyticsService analyticsService;
    private final AgentDatasetService datasetService;
    private final AgentSessionStateService stateService;

    public QueryBusinessDataTool(AgentToolPermissionService permissionService, MesAnalyticsService analyticsService,
        AgentDatasetService datasetService, AgentSessionStateService stateService)
    {
        this.permissionService = permissionService;
        this.analyticsService = analyticsService;
        this.datasetService = datasetService;
        this.stateService = stateService;
    }

    @Override
    public AgentToolCode code()
    {
        return AgentToolCode.QUERY_BUSINESS_DATA;
    }

    @Override
    public AgentStepType stepType()
    {
        return AgentStepType.DATA_QUERY;
    }

    @Override
    public String displayName()
    {
        return "正在查询业务数据";
    }

    /** 第一版支持物料按分类、类型或状态聚合计数。 */
    @Override
    public AgentToolResult execute(AgentToolContext context, Map<String, Object> input)
    {
        requireValue(input, "domain", MATERIAL_DOMAIN, "第一版仅支持material业务域");
        requireValue(input, "operation", COUNT_OPERATION, "第一版仅支持count统计");
        permissionService.require(context.userId(), MATERIAL_LIST_PERMISSION);

        MaterialStatisticsQuery query = new MaterialStatisticsQuery();
        query.setDimension(parseDimension(text(input.get("groupBy"))));
        Map<String, Object> filters = map(input.get("filters"));
        query.setMaterialType(parseMaterialType(text(filters.get("materialType"))));
        query.setCategoryId(longValue(filters.get("categoryId")));
        query.setStatus(optionalUpperText(filters.get("status")));

        List<MaterialStatisticsRow> statistics = analyticsService.materialStatistics(query);
        List<Map<String, Object>> rows = statistics.stream().map(this::toRow).toList();
        AgentDataset dataset = datasetService.createInline(context.userId(), context.conversationId(),
            context.runId(), null, datasetName(query.getDimension()), rows, context.username());
        String goal = text(input.get("goal"));
        stateService.rememberGoal(context.conversationId(),
            goal.isBlank() ? dataset.getDatasetName() : goal, MATERIAL_DOMAIN, context.username());
        stateService.rememberDataset(context.conversationId(), dataset.getId(), context.username());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("datasetId", dataset.getId());
        data.put("datasetName", dataset.getDatasetName());
        data.put("rowCount", dataset.getRowCount());
        data.put("columns", List.of("dimensionCode", "dimensionName", "materialCount"));
        AgentToolResult result = AgentToolResult.success("已完成物料统计，共" + rows.size() + "组数据", data);
        result.setNextActions(List.of("分析数据", "生成图表"));
        return result;
    }

    private MaterialStatisticsDimension parseDimension(String value)
    {
        String normalized = value.replace("_", "").replace("-", "").toUpperCase(Locale.ROOT);
        return switch (normalized)
        {
            case "CATEGORY" -> MaterialStatisticsDimension.CATEGORY;
            case "MATERIALTYPE", "TYPE" -> MaterialStatisticsDimension.MATERIAL_TYPE;
            case "STATUS" -> MaterialStatisticsDimension.STATUS;
            default -> throw new ServiceException("不支持的物料统计维度：" + value);
        };
    }

    private String parseMaterialType(String value)
    {
        if (value.isBlank())
        {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        return Arrays.stream(MaterialType.values()).filter(type -> type.getCode().equals(normalized))
            .findFirst().orElseThrow(() -> new ServiceException("不支持的物料类型：" + value)).getCode();
    }

    private Map<String, Object> toRow(MaterialStatisticsRow source)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dimensionCode", source.getDimensionCode());
        row.put("dimensionName", source.getDimensionName());
        row.put("materialCount", source.getMaterialCount());
        return row;
    }

    private String datasetName(MaterialStatisticsDimension dimension)
    {
        return switch (dimension)
        {
            case CATEGORY -> "物料分类数量统计";
            case MATERIAL_TYPE -> "物料类型数量统计";
            case STATUS -> "物料状态数量统计";
        };
    }

    private void requireValue(Map<String, Object> input, String key, String expected, String message)
    {
        if (!expected.equalsIgnoreCase(text(input.get(key))))
        {
            throw new ServiceException(message);
        }
    }

    private String text(Object value)
    {
        return value instanceof String text ? text.trim() : "";
    }

    private String optionalUpperText(Object value)
    {
        String text = text(value);
        return text.isBlank() ? null : text.toUpperCase(Locale.ROOT);
    }

    private Long longValue(Object value)
    {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try
        {
            return Long.valueOf(value.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("categoryId必须是数字");
        }
    }

    private Map<String, Object> map(Object value)
    {
        if (!(value instanceof Map<?, ?> source))
        {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }
}
