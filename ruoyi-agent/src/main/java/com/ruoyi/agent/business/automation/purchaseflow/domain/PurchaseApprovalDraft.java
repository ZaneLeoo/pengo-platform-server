package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 采购单据待审核确认数据。 */
@Data
public class PurchaseApprovalDraft {
    private String actionType;
    private Long documentId;
    private String documentCode;
    private String documentStatus;
    private String supplierCode;
    private String supplierName;
    private String documentDate;
    private BigDecimal totalQuantity;
    private String impactMessage;
    private List<PurchaseApprovalLine> lines;
}
