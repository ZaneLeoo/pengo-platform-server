package com.ruoyi.agent.business.automation.domain;

import java.util.List;

/** 采购订单准备工具的结构化响应。 */
public record PurchaseOrderPreparationResult(
    AutomationPreparationStatus status,
    String message,
    List<String> missingFields,
    List<AutomationCandidate> candidates,
    PurchaseOrderDraft draft)
{
}
