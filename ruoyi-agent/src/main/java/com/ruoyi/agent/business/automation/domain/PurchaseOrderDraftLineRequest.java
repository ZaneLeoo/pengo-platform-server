package com.ruoyi.agent.business.automation.domain;

import java.math.BigDecimal;

/** AI 准备采购订单草稿的一行输入。 */
public record PurchaseOrderDraftLineRequest(
        String materialKeyword,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String plannedDate,
        Long quoteId,
        Long quoteLineId,
        String priceSource) {
    /** 兼容未使用供应商报价的普通采购订单草稿请求。 */
    public PurchaseOrderDraftLineRequest(String materialKeyword, BigDecimal quantity, BigDecimal unitPrice,
            String plannedDate) {
        this(materialKeyword, quantity, unitPrice, plannedDate, null, null, "MANUAL");
    }
}
