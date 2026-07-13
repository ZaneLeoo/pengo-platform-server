package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;

/** 返回给 AI 的可比较报价候选项。 */
public record PurchaseQuoteCandidate(
    String materialCode,
    Long supplierId,
    String supplierCode,
    String supplierName,
    Long quoteId,
    Long quoteLineId,
    String quoteCode,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    boolean taxIncluded,
    String currency,
    BigDecimal comparableUnitPrice,
    BigDecimal totalAmount,
    Integer leadTimeDays,
    String expireDate)
{
}
