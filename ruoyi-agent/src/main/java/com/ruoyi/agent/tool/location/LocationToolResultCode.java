package com.ruoyi.agent.tool.location;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 库位查询工具结果码。 */
public enum LocationToolResultCode implements AgentToolResultCode {
    LOCATION_QUERY_SUCCESS, LOCATION_NOT_FOUND;

    @Override
    public String code() {
        return name();
    }
}
