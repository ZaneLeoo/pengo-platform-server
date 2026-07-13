package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** 后端根据主数据标准化后的采购订单草稿。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDraft {
    private String supplierCode;
    private String supplierName;
    private String currency;
    private String orderDate;
    private String expectedDate;
    private String remark;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private List<PurchaseOrderDraftLine> lines;

}
