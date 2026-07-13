package com.ruoyi.agent.tool.shared;

/** 所有 Agent 工具共享的基础结果码。 */
public enum CommonAgentToolResultCode implements AgentToolResultCode {
    TOOL_UNAUTHORIZED, INVALID_REQUEST, BUSINESS_REJECTED, INTERNAL_ERROR;

    /** 返回枚举名称作为稳定结果码。 */
    @Override
    public String code() {
        return name();
    }
}
