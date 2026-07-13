package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 采购订单准备工具的结构化响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderPreparationResult {
    private AutomationPreparationStatus status;
    private String message;
    private List<String> missingFields;
    private List<AutomationCandidate> candidates;
    private PurchaseOrderDraft draft;

}
