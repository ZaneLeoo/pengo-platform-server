package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 单个物料的供应商报价推荐结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteRecommendation {
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long quoteId;
    private Long quoteLineId;
    private String quoteCode;
    private BigDecimal unitPrice;
    private BigDecimal orderUnitPrice;
    private BigDecimal taxRate;
    private boolean taxIncluded;
    private String currency;
    private BigDecimal comparableUnitPrice;
    private BigDecimal totalAmount;
    private Integer leadTimeDays;
    private String expireDate;
    private String reason;

}
