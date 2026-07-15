package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.math.BigDecimal;
import lombok.Data;

/** AI 准备采购入库草稿时提交的来源到货行。 */
@Data
public class InboundDraftLineRequest {
    private Long sourceReceiptLineId;
    private BigDecimal inboundQuantity;
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
}
