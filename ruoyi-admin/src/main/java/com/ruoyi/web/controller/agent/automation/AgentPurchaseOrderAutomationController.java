package com.ruoyi.web.controller.agent.automation;

import com.ruoyi.agent.business.automation.application.PurchaseOrderAutomationService;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftResult;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前登录用户确认后的 AI 采购业务写入口。 */
@RestController
@RequestMapping("/agent/automation/purchase-orders")
public class AgentPurchaseOrderAutomationController extends BaseController {
    private final PurchaseOrderAutomationService automationService;

    public AgentPurchaseOrderAutomationController(PurchaseOrderAutomationService automationService) {
        this.automationService = automationService;
    }

    /** 用户确认后创建采购订单草稿。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:add')")
    @Log(title = "AI 创建采购订单草稿", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult createPurchaseOrderDraft(@RequestBody CreatePurchaseOrderDraftRequest request) {
        CreatePurchaseOrderDraftResult result = automationService.createDraft(request, getUserId(), getUsername());
        return success(result);
    }
}
