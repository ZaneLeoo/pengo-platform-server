package com.ruoyi.agent.tool.inventory;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 库存工具结果码。 */
public enum InventoryToolResultCode implements AgentToolResultCode {
    INVENTORY_BALANCE_QUERY_SUCCESS, INVENTORY_BALANCE_NOT_FOUND, INVENTORY_TRANSACTION_QUERY_SUCCESS, INVENTORY_TRANSACTION_NOT_FOUND;

    @Override
    public String code() {
        return name();
    }
}
