package com.ruoyi.agent.tool.purchaseflow;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 采购闭环工具结果码。 */
public enum PurchaseFlowToolResultCode implements AgentToolResultCode {
    RECEIPT_CANDIDATES_FOUND,
    RECEIPT_DRAFT_READY,
    INSPECTION_DRAFT_READY,
    INBOUND_CANDIDATES_FOUND,
    INBOUND_DRAFT_READY,
    PURCHASE_FLOW_VALIDATION_FAILED;

    @Override
    public String code() {
        return name();
    }
}
