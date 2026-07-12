package com.ruoyi.agent.business.automation.domain;

import java.util.List;

/** AI 准备采购订单草稿时允许传入的不完整业务信息。 */
public record PurchaseOrderDraftRequest(
    String supplierKeyword,
    String orderDate,
    String expectedDate,
    String remark,
    List<PurchaseOrderDraftLineRequest> lines)
{
}
