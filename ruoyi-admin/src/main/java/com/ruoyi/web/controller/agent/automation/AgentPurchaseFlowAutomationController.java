package com.ruoyi.web.controller.agent.automation;

import com.ruoyi.agent.business.automation.purchaseflow.application.PurchaseFlowAutomationService;
import com.ruoyi.agent.business.automation.purchaseflow.application.PurchaseApprovalAutomationService;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ConfirmedActionRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.DocumentActionRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraft;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录用户确认后的采购闭环写入口。 */
@RestController
@RequestMapping("/agent/automation/purchase-flow")
public class AgentPurchaseFlowAutomationController extends BaseController {
    private final PurchaseFlowAutomationService automationService;
    private final PurchaseApprovalAutomationService approvalService;

    public AgentPurchaseFlowAutomationController(PurchaseFlowAutomationService automationService,
            PurchaseApprovalAutomationService approvalService) {
        this.automationService = automationService;
        this.approvalService = approvalService;
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:add')")
    @Log(title = "AI 创建采购到货草稿", businessType = BusinessType.INSERT)
    @PostMapping("/receipts")
    public AjaxResult createReceipt(@RequestBody ConfirmedActionRequest<ReceiptDraft> request) {
        return success(automationService.createReceipt(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:inspect')")
    @Log(title = "AI 提交采购到货质检", businessType = BusinessType.UPDATE)
    @PostMapping("/inspections")
    public AjaxResult inspectReceipt(@RequestBody ConfirmedActionRequest<InspectionDraft> request) {
        return success(automationService.inspectReceipt(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:add')")
    @Log(title = "AI 创建采购入库草稿", businessType = BusinessType.INSERT)
    @PostMapping("/inbounds")
    public AjaxResult createInbound(@RequestBody ConfirmedActionRequest<InboundDraft> request) {
        return success(automationService.createInbound(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:approve')")
    @Log(title = "AI 审核采购订单", businessType = BusinessType.UPDATE)
    @PostMapping("/orders/approve")
    public AjaxResult approveOrder(@RequestBody DocumentActionRequest request) {
        return success(approvalService.approveOrder(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:approve')")
    @Log(title = "AI 审核采购到货单", businessType = BusinessType.UPDATE)
    @PostMapping("/receipts/approve")
    public AjaxResult approveReceipt(@RequestBody DocumentActionRequest request) {
        return success(approvalService.approveReceipt(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:approve')")
    @Log(title = "AI 审核采购入库单", businessType = BusinessType.UPDATE)
    @PostMapping("/inbounds/approve")
    public AjaxResult approveInbound(@RequestBody DocumentActionRequest request) {
        return success(approvalService.approveInbound(request, getUserId(), getUsername()));
    }

}
