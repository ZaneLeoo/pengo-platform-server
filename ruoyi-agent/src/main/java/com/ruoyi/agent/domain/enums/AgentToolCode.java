package com.ruoyi.agent.domain.enums;

import java.util.Arrays;

/** 第一版 Supervisor 可调用的工具编码。 */
public enum AgentToolCode
{
    KNOWLEDGE_SEARCH("knowledge_search"),
    QUERY_BUSINESS_DATA("query_business_data"),
    ANALYZE_DATASET("analyze_dataset"),
    RENDER_CHART("render_chart"),
    REQUEST_CLARIFICATION("request_clarification");

    private final String code;

    AgentToolCode(String code)
    {
        this.code = code;
    }

    /** 返回提供给 Dify 的稳定工具编码。 */
    public String getCode()
    {
        return code;
    }

    /** 按外部工具编码解析枚举。 */
    public static AgentToolCode fromCode(String code)
    {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("不支持的Agent工具：" + code));
    }
}
