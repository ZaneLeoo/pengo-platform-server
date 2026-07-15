package com.ruoyi.agent.business.automation.purchaseflow.domain;

import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 等待用户确认的采购到货草稿。 */
@Data
public class ReceiptDraft {
    private String actionType = "PURCHASE_RECEIPT_CREATE";
    private String receiptDate;
    private String supplierCode;
    private String supplierName;
    private String remark;
    private BigDecimal totalQuantity;
    private List<PurchaseReceiptLine> lines;
}
