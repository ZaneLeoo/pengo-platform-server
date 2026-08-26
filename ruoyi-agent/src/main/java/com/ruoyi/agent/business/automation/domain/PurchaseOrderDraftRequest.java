package com.ruoyi.agent.business.automation.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 准备采购订单草稿时允许传入的不完整业务信息。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDraftRequest {
    private String supplierKeyword;
    private String orderDate;
    private String expectedDate;
    private String remark;
    private List<PurchaseOrderDraftLineRequest> lines;
}
