package com.ruoyi.agent.business.automation.domain;

import java.math.BigDecimal;
import java.util.List;

/** 后端根据主数据标准化后的采购订单草稿。 */
public record PurchaseOrderDraft(
    String supplierCode,
    String supplierName,
    String currency,
    String orderDate,
    String expectedDate,
    String remark,
    BigDecimal totalQuantity,
    BigDecimal totalAmount,
    List<PurchaseOrderDraftLine> lines)
{
}
