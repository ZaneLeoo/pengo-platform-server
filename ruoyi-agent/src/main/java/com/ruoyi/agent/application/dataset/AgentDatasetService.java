package com.ruoyi.agent.application.dataset;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.agent.domain.runtime.AgentDataset;
import com.ruoyi.agent.mapper.runtime.AgentDatasetMapper;
import com.ruoyi.common.exception.ServiceException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 创建和读取可跨轮次复用的受控数据集。 */
@Service
public class AgentDatasetService
{
    public static final int MAX_INLINE_ROWS = 1000;
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final String STORAGE_INLINE_JSON = "INLINE_JSON";

    private final AgentDatasetMapper datasetMapper;

    public AgentDatasetService(AgentDatasetMapper datasetMapper)
    {
        this.datasetMapper = datasetMapper;
    }

    /** 保存第一版内联 JSON 数据集，并自动推断字段结构。 */
    public AgentDataset createInline(Long ownerUserId, Long conversationId, Long runId, Long sourceToolCallId,
        String name, List<Map<String, Object>> rows, String username)
    {
        List<Map<String, Object>> safeRows = rows == null ? List.of() : rows;
        if (safeRows.size() > MAX_INLINE_ROWS)
        {
            throw new ServiceException("数据集最多允许" + MAX_INLINE_ROWS + "行，请缩小查询范围");
        }
        AgentDataset dataset = new AgentDataset();
        dataset.setOwnerUserId(ownerUserId);
        dataset.setConversationId(conversationId);
        dataset.setRunId(runId);
        dataset.setSourceToolCallId(sourceToolCallId);
        dataset.setDatasetName(name);
        dataset.setStorageType(STORAGE_INLINE_JSON);
        dataset.setSchemaJson(JSON.toJSONString(inferSchema(safeRows)));
        dataset.setDataJson(JSON.toJSONString(safeRows));
        dataset.setRowCount(safeRows.size());
        dataset.setExpireTime(Date.from(Instant.now().plus(DEFAULT_TTL)));
        dataset.setCreateBy(username);
        datasetMapper.insert(dataset);
        return dataset;
    }

    /** 仅允许数据集所属用户读取尚未过期的数据。 */
    public AgentDataset requireOwned(Long datasetId, Long userId)
    {
        AgentDataset dataset = datasetMapper.selectOwned(datasetId, userId);
        if (dataset == null)
        {
            throw new ServiceException("数据集不存在、已过期或无权访问");
        }
        return dataset;
    }

    /** 将内联数据解析成保持列顺序的行集合。 */
    public List<Map<String, Object>> rows(AgentDataset dataset)
    {
        if (dataset == null || dataset.getDataJson() == null)
        {
            return List.of();
        }
        return JSON.parseObject(dataset.getDataJson(),
            new TypeReference<List<Map<String, Object>>>() { });
    }

    private List<Map<String, String>> inferSchema(List<Map<String, Object>> rows)
    {
        if (rows.isEmpty())
        {
            return List.of();
        }
        List<Map<String, String>> schema = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rows.get(0).entrySet())
        {
            Map<String, String> field = new LinkedHashMap<>();
            field.put("name", entry.getKey());
            field.put("type", typeOf(entry.getValue()));
            schema.add(field);
        }
        return schema;
    }

    private String typeOf(Object value)
    {
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        return "string";
    }
}
