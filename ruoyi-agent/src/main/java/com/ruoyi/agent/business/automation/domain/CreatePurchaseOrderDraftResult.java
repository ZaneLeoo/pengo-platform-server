package com.ruoyi.agent.business.automation.domain;

/** 创建采购订单草稿的结果。 */
public record CreatePurchaseOrderDraftResult(Long orderId, String orderCode, boolean duplicated)
{
}
