package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 标准化采购订单草稿明细。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDraftLine {
    private Integer lineNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String spec;
    private String model;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private String plannedDate;
    private Long quoteId;
    private Long quoteLineId;
    private String priceSource;

}
