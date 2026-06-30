package com.ruoyi.agent.application.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 Dify 知识检索来源归一为前端稳定 sources 协议。 */
@org.springframework.stereotype.Component
public class SourceExtractor
{
    private static final int MAX_CONTENT_LENGTH = 500;

    /** 从 message_end.metadata.retriever_resources 中提取来源。 */
    public List<AgentSource> extractFromMetadata(Map<String, Object> metadata)
    {
        if (metadata == null)
        {
            return Collections.emptyList();
        }
        return fromItems(toList(metadata.get("retriever_resources")), false);
    }

    /** 从 knowledge-retrieval 节点 data.outputs.result 中提取来源。 */
    public List<AgentSource> extractFromNodeData(Map<String, Object> data)
    {
        if (data == null)
        {
            return Collections.emptyList();
        }
        Object outputsValue = data.get("outputs");
        if (!(outputsValue instanceof Map<?, ?> outputs))
        {
            return Collections.emptyList();
        }
        return fromItems(toList(outputs.get("result")), true);
    }

    private List<AgentSource> fromItems(List<?> items, boolean nestedMetadata)
    {
        List<AgentSource> sources = new ArrayList<>();
        for (Object item : items)
        {
            if (!(item instanceof Map<?, ?> sourceMap))
            {
                continue;
            }
            Map<String, Object> source = stringKeyMap(sourceMap);
            Map<String, Object> metadata = nestedMetadata && source.get("metadata") instanceof Map<?, ?> nested
                ? stringKeyMap(nested) : source;

            String segmentId = text(metadata.get("segment_id"));
            Integer position = integer(metadata.get("position"));
            String id = firstText(segmentId, text(metadata.get("document_id")), position == null ? null : String.valueOf(position));
            String documentName = text(metadata.get("document_name"));
            String datasetName = text(metadata.get("dataset_name"));
            String title = firstText(documentName, text(source.get("title")), datasetName, "知识来源");
            String content = firstText(text(source.get("content")), text(metadata.get("content")));

            if (content == null || content.isBlank())
            {
                continue;
            }

            sources.add(new AgentSource(id, title, "knowledge", documentName, datasetName, truncate(content),
                decimal(metadata.get("score")), position, segmentId, integer(metadata.get("page"))));
        }
        return sources;
    }

    private String truncate(String value)
    {
        return value.length() > MAX_CONTENT_LENGTH ? value.substring(0, MAX_CONTENT_LENGTH) : value;
    }

    private List<?> toList(Object value)
    {
        return value instanceof List<?> list ? list : Collections.emptyList();
    }

    private Map<String, Object> stringKeyMap(Map<?, ?> source)
    {
        Map<String, Object> target = new LinkedHashMap<>();
        source.forEach((key, value) -> target.put(String.valueOf(key), value));
        return target;
    }

    private String firstText(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.isBlank())
            {
                return value;
            }
        }
        return null;
    }

    private String text(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private Double decimal(Object value)
    {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private Integer integer(Object value)
    {
        return value instanceof Number number ? number.intValue() : null;
    }
}
