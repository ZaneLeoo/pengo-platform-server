package com.ruoyi.agent.tool.purchaseapproval;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 采购审核准备工具结果码。 */
public enum PurchaseApprovalToolResultCode implements AgentToolResultCode {
    PURCHASE_APPROVAL_READY, PURCHASE_APPROVAL_REJECTED;

    @Override
    public String code() { return name(); }
}
