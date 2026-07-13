package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** AI 准备采购订单草稿的一行输入。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDraftLineRequest {
    private String materialKeyword;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String plannedDate;
    private Long quoteId;
    private Long quoteLineId;
    private String priceSource;

    /** 兼容未使用供应商报价的普通采购订单草稿请求。 */
    public PurchaseOrderDraftLineRequest(String materialKeyword, BigDecimal quantity, BigDecimal unitPrice,
            String plannedDate) {
        this(materialKeyword, quantity, unitPrice, plannedDate, null, null, "MANUAL");
    }
}
