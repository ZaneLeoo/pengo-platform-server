package com.ruoyi.agent.tool.warehouse;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 仓库查询工具结果码。 */
public enum WarehouseToolResultCode implements AgentToolResultCode {
    WAREHOUSE_QUERY_SUCCESS, WAREHOUSE_NOT_FOUND;

    @Override
    public String code() {
        return name();
    }
}
