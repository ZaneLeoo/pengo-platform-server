package com.ruoyi.agent.business.automation.domain;

import java.math.BigDecimal;

/** 标准化采购订单草稿明细。 */
public record PurchaseOrderDraftLine(
        Integer lineNo,
        Long materialId,
        String materialCode,
        String materialName,
        String spec,
        String model,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal amount,
        String plannedDate,
        Long quoteId,
        Long quoteLineId,
        String priceSource) {
}
