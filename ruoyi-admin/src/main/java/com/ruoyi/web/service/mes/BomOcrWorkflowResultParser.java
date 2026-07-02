package com.ruoyi.web.service.mes;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.ocr.BomOcrResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 将 Dify 工作流输出解析为 BOM OCR 结构化结果。 */
@Component
public class BomOcrWorkflowResultParser
{
    private static final List<String> RESULT_KEYS = List.of("result", "json", "output", "text", "answer");

    /** 解析 Dify outputs，兼容字符串 JSON、代码块 JSON 和直接对象输出。 */
    public BomOcrResult parse(Map<String, Object> outputs)
    {
        if (outputs == null || outputs.isEmpty())
        {
            throw new ServiceException("Dify 未返回 BOM 结构化结果");
        }
        if (looksLikeBomResult(outputs))
        {
            return read(JSON.toJSONString(outputs));
        }
        for (String key : RESULT_KEYS)
        {
            Object value = outputs.get(key);
            if (value != null)
            {
                return parseValue(value);
            }
        }
        if (outputs.size() == 1)
        {
            return parseValue(outputs.values().iterator().next());
        }
        throw new ServiceException("Dify 输出中未找到 result/json/text 等结构化结果字段");
    }

    private BomOcrResult parseValue(Object value)
    {
        if (value instanceof Map<?, ?> || value instanceof List<?>)
        {
            return read(JSON.toJSONString(value));
        }
        String text = stripJsonFence(String.valueOf(value));
        if (StringUtils.isBlank(text))
        {
            throw new ServiceException("Dify BOM 结构化结果为空");
        }
        return read(text);
    }

    private boolean looksLikeBomResult(Map<String, Object> outputs)
    {
        return outputs.containsKey("document") || outputs.containsKey("items") || outputs.containsKey("version");
    }

    private BomOcrResult read(String json)
    {
        try
        {
            BomOcrResult result = JSON.parseObject(json, BomOcrResult.class);
            if (result.getDocument() == null && (result.getItems() == null || result.getItems().isEmpty()))
            {
                throw new ServiceException("Dify BOM 结构化结果缺少 document/items");
            }
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("Dify BOM 结构化结果不是合法 JSON：" + e.getMessage());
        }
    }

    private String stripJsonFence(String value)
    {
        String text = value == null ? "" : value.trim();
        if (text.startsWith("```"))
        {
            int firstLineEnd = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd)
            {
                return text.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        int start = firstJsonStart(text);
        int end = Math.max(text.lastIndexOf('}'), text.lastIndexOf(']'));
        if (start >= 0 && end >= start)
        {
            return text.substring(start, end + 1).trim();
        }
        return text;
    }

    private int firstJsonStart(String text)
    {
        int objectStart = text.indexOf('{');
        int arrayStart = text.indexOf('[');
        if (objectStart < 0) return arrayStart;
        if (arrayStart < 0) return objectStart;
        return Math.min(objectStart, arrayStart);
    }
}
