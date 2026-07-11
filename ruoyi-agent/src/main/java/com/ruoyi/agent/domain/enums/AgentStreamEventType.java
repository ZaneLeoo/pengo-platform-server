package com.ruoyi.agent.domain.enums;

import lombok.Getter;

/** Agent 对外 SSE 事件类型。前后端通过这些稳定名称协作，不直接暴露 Dify 原始事件。 */
@Getter
public enum AgentStreamEventType
{
    MESSAGE("message"),
    METADATA("metadata"),
    TOOL("tool"),
    KNOWLEDGE("knowledge"),
    DONE("done"),
    ERROR("error");

    private final String value;

    AgentStreamEventType(String value)
    {
        this.value = value;
    }

}
