package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户确认后，agent-ui 提交的采购订单草稿创建请求。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderDraftRequest {
    private String requestId;
    private PurchaseOrderDraft draft;

}
