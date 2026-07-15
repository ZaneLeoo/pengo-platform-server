package com.ruoyi.agent.business.automation.purchaseflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 采购闭环自动化写动作结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationDocumentResult {
    private Long documentId;
    private String documentCode;
    private String status;
    private boolean duplicated;
}
