package com.ruoyi.agent.business.automation.purchaseflow.domain;

import com.ruoyi.mes.purchase.domain.dto.InspectionLineRequest;
import java.util.List;
import lombok.Data;

/** 等待用户确认的采购到货质检草稿。 */
@Data
public class InspectionDraft {
    private String actionType = "PURCHASE_RECEIPT_INSPECT";
    private Long receiptId;
    private String receiptCode;
    private String supplierName;
    private List<InspectionLineRequest> lines;
}
