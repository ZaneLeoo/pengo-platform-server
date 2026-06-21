package com.ruoyi.agent.application.artifact;

import com.alibaba.fastjson2.JSON;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 从 Dify 工作流输出中提取通过校验的结构化产物。 */
@org.springframework.stereotype.Component
public class ArtifactExtractor
{
    private final ArtifactValidator validator;

    public ArtifactExtractor(ArtifactValidator validator)
    {
        this.validator = validator;
    }

    /** 从 outputs 中提取结构化产物，支持多态兼容性处理。 */
    public List<AgentArtifact> extract(Map<String, Object> data)
    {
        Object outputsValue = data == null ? null : data.get("outputs");
        List<?> items = Collections.emptyList();

        if (outputsValue instanceof List<?> list)
        {
            // 情况A：outputs直接就是List数组（比如集成Chatflow中子工作流的合并输出）
            items = list;
        }
        else if (outputsValue instanceof Map<?, ?> outputs)
        {
            // 情况B：outputs是Map结构，优先取artifacts键，无则尝试读outputs键以兼容双重起名习惯
            items = toList(outputs.get("artifacts"));
            if (items.isEmpty())
            {
                items = toList(outputs.get("outputs"));
            }
        }
        else if (outputsValue instanceof String jsonStr)
        {
            // 情况C：如果outputs是个JSON字符串，自动解析为List列表
            items = toList(jsonStr);
        }

        List<AgentArtifact> result = new ArrayList<>();
        for (Object item : items)
        {
            if (item instanceof Map<?, ?> source)
            {
                Map<String, Object> artifact = stringKeyMap(source);
                if (validator.isValid(artifact))
                {
                    Map<String, Object> payload = stringKeyMap((Map<?, ?>) artifact.get("payload"));
                    normalizeChartCategories(artifact, payload);
                    result.add(new AgentArtifact(String.valueOf(artifact.get("type")),
                        String.valueOf(artifact.get("version")), String.valueOf(artifact.get("title")), payload));
                }
            }
        }
        return result;
    }

    /** 将历史 xAxis 字段归一为 1.0 协议中的 categories 字段。 */
    private void normalizeChartCategories(Map<String, Object> artifact, Map<String, Object> payload)
    {
        if ("chart".equals(artifact.get("type")) && !payload.containsKey("categories")
            && payload.get("xAxis") instanceof List<?>)
        {
            payload.put("categories", payload.get("xAxis"));
            payload.remove("xAxis");
        }
    }

    /** 将 Dify 可能返回的数组或 JSON 字符串统一成列表。 */
    private List<?> toList(Object value)
    {
        if (value instanceof List<?> list)
        {
            return list;
        }
        if (value instanceof String json && !json.isBlank())
        {
            try
            {
                return JSON.parseArray(json, Object.class);
            }
            catch (RuntimeException ignored)
            {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    /** 复制为只使用字符串键的协议 Map。 */
    private Map<String, Object> stringKeyMap(Map<?, ?> source)
    {
        Map<String, Object> target = new LinkedHashMap<>();
        source.forEach((key, value) -> target.put(String.valueOf(key), value));
        return target;
    }
}
