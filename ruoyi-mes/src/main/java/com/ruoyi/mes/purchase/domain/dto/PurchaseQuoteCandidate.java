package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 返回给 AI 的可比较报价候选项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteCandidate {
    private String materialCode;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long quoteId;
    private Long quoteLineId;
    private String quoteCode;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private boolean taxIncluded;
    private String currency;
    private BigDecimal comparableUnitPrice;
    private BigDecimal totalAmount;
    private Integer leadTimeDays;
    private String expireDate;

}
