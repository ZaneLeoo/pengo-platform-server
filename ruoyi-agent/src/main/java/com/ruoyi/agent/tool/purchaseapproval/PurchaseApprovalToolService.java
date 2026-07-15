package com.ruoyi.agent.tool.purchaseapproval;

import com.ruoyi.agent.business.automation.purchaseflow.application.PurchaseApprovalAutomationService;
import com.ruoyi.agent.business.automation.purchaseflow.domain.PurchaseApprovalDraft;
import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.common.exception.ServiceException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Dify 采购单据审核准备工具适配层。 */
@Service
public class PurchaseApprovalToolService {
    private final PurchaseApprovalAutomationService automationService;

    public PurchaseApprovalToolService(PurchaseApprovalAutomationService automationService) {
        this.automationService = automationService;
    }

    public AgentToolResult<PurchaseApprovalDraft> prepareOrder(String documentCode) {
        return prepare(() -> automationService.prepareOrder(documentCode));
    }

    public AgentToolResult<PurchaseApprovalDraft> prepareReceipt(String documentCode) {
        return prepare(() -> automationService.prepareReceipt(documentCode));
    }

    public AgentToolResult<PurchaseApprovalDraft> prepareInbound(String documentCode) {
        return prepare(() -> automationService.prepareInbound(documentCode));
    }

    private AgentToolResult<PurchaseApprovalDraft> prepare(Supplier supplier) {
        try {
            PurchaseApprovalDraft draft = supplier.get();
            return AgentToolResults.confirm(PurchaseApprovalToolResultCode.PURCHASE_APPROVAL_READY,
                    "单据审核信息已准备", draft, "由 agent-ui 展示审核确认卡片；未获得用户确认前禁止审核。");
        } catch (ServiceException exception) {
            AgentToolIssue issue = AgentToolIssue.of("APPROVAL_VALIDATION_FAILED", "documentCode",
                    exception.getMessage(), "核对单据编号、状态和审核前置信息");
            return AgentToolResults.rejected(PurchaseApprovalToolResultCode.PURCHASE_APPROVAL_REJECTED,
                    exception.getMessage(), List.of(issue), null);
        }
    }

    @FunctionalInterface
    private interface Supplier { PurchaseApprovalDraft get(); }
}
