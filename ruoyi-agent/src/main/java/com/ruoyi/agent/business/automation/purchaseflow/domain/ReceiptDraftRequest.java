package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.util.List;
import lombok.Data;

/** AI 准备采购到货草稿的请求。 */
@Data
public class ReceiptDraftRequest {
    private String receiptDate;
    private String warehouseCode;
    private String warehouseName;
    private String remark;
    private List<ReceiptDraftLineRequest> lines;
}
