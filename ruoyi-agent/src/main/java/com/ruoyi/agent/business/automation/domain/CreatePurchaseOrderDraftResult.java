package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 创建采购订单草稿的结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderDraftResult {
    private Long orderId;
    private String orderCode;
    private boolean duplicated;

}
