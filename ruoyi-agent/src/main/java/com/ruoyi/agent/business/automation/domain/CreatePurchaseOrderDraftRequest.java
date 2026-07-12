package com.ruoyi.agent.business.automation.domain;

/** 用户确认后，agent-ui 提交的采购订单草稿创建请求。 */
public record CreatePurchaseOrderDraftRequest(String requestId, PurchaseOrderDraft draft)
{
}
