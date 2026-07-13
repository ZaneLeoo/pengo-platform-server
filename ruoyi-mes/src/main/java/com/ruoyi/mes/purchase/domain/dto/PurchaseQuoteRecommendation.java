package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;

/** 单个物料的供应商报价推荐结果。 */
public record PurchaseQuoteRecommendation(
        String materialCode,
        String materialName,
        BigDecimal quantity,
        Long supplierId,
        String supplierCode,
        String supplierName,
        Long quoteId,
        Long quoteLineId,
        String quoteCode,
        BigDecimal unitPrice,
        BigDecimal orderUnitPrice,
        BigDecimal taxRate,
        boolean taxIncluded,
        String currency,
        BigDecimal comparableUnitPrice,
        BigDecimal totalAmount,
        Integer leadTimeDays,
        String expireDate,
        String reason) {
}
