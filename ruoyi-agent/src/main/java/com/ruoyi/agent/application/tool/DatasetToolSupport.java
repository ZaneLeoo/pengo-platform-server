package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.application.dataset.AgentDatasetService;
import com.ruoyi.agent.domain.runtime.AgentDataset;
import com.ruoyi.common.exception.ServiceException;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 数据分析和图表工具共享的数据集解析规则。 */
@Component
public class DatasetToolSupport
{
    private final AgentDatasetService datasetService;

    public DatasetToolSupport(AgentDatasetService datasetService)
    {
        this.datasetService = datasetService;
    }

    /** 优先使用显式 datasetId，否则复用会话最近数据集。 */
    public AgentDataset requireDataset(AgentToolContext context, Map<String, Object> input)
    {
        Long datasetId = longValue(input.get("datasetId"));
        if (datasetId == null)
        {
            datasetId = context.sessionState().getLastDatasetId();
        }
        if (datasetId == null)
        {
            throw new ServiceException("当前会话没有可复用的数据，请先查询数据");
        }
        return datasetService.requireOwned(datasetId, context.userId());
    }

    public List<Map<String, Object>> rows(AgentDataset dataset)
    {
        return datasetService.rows(dataset);
    }

    /** 获取所有字段并保持查询结果原顺序。 */
    public Set<String> fields(List<Map<String, Object>> rows)
    {
        Set<String> fields = new LinkedHashSet<>();
        rows.forEach(row -> fields.addAll(row.keySet()));
        return fields;
    }

    /** 将受控数值字段转换为 BigDecimal。 */
    public BigDecimal decimal(Object value, String field)
    {
        if (value instanceof Number number)
        {
            return new BigDecimal(number.toString());
        }
        if (value instanceof String text)
        {
            try
            {
                return new BigDecimal(text.trim());
            }
            catch (NumberFormatException ignored)
            {
                // 统一在下面返回业务错误。
            }
        }
        throw new ServiceException("字段" + field + "包含非数值数据，无法分析或绘图");
    }

    public Long longValue(Object value)
    {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try
        {
            return Long.valueOf(value.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("datasetId必须是数字");
        }
    }
}
