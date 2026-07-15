package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.math.BigDecimal;
import lombok.Data;

/** 采购单据审核确认卡片明细。 */
@Data
public class PurchaseApprovalLine {
    private Integer lineNo;
    private String sourceOrderCode;
    private String sourceReceiptCode;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal quantity;
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
    private String lotNo;
}
