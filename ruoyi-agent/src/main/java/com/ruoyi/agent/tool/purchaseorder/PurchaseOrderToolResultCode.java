package com.ruoyi.agent.tool.purchaseorder;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 采购订单准备工具结果码。 */
public enum PurchaseOrderToolResultCode implements AgentToolResultCode
{
    MISSING_REQUIRED_FIELDS,
    AMBIGUOUS_MASTER_DATA,
    BUSINESS_VALIDATION_FAILED,
    PURCHASE_ORDER_DRAFT_READY;

    @Override
    public String code()
    {
        return name();
    }
}
