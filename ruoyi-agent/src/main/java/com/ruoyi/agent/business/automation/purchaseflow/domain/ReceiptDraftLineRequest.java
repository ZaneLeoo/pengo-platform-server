package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.math.BigDecimal;
import lombok.Data;

/** AI 准备采购到货草稿时提交的来源订单行。 */
@Data
public class ReceiptDraftLineRequest {
    private Long sourceOrderLineId;
    private BigDecimal receivedQuantity;
    private String lotNo;
    private String productionDate;
    private String expiryDate;
    private String locationCode;
    private String locationName;
}
