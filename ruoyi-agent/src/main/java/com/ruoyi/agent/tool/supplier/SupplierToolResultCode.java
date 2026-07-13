package com.ruoyi.agent.tool.supplier;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 供应商工具结果码。 */
public enum SupplierToolResultCode implements AgentToolResultCode
{
    SUPPLIER_QUERY_SUCCESS,
    SUPPLIER_NOT_FOUND;

    @Override
    public String code()
    {
        return name();
    }
}
