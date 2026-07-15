package com.ruoyi.agent.business.automation.purchaseflow.domain;

import com.ruoyi.mes.purchase.domain.dto.InspectionLineRequest;
import java.util.List;
import lombok.Data;

/** AI 准备采购到货质检结果的请求。 */
@Data
public class InspectionDraftRequest {
    private Long receiptId;
    private String receiptCode;
    private List<InspectionLineRequest> lines;
}
