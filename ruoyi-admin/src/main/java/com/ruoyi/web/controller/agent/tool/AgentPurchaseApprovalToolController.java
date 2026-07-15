package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.business.automation.purchaseflow.domain.PurchaseApprovalDraft;
import com.ruoyi.agent.tool.purchaseapproval.PurchaseApprovalToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 采购单据审核准备工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/purchase-approvals")
public class AgentPurchaseApprovalToolController {
    private final PurchaseApprovalToolService toolService;

    public AgentPurchaseApprovalToolController(PurchaseApprovalToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping("/orders/prepare")
    public AgentToolResult<PurchaseApprovalDraft> prepareOrder(
            @RequestBody(required = false) Map<String, String> request) {
        return toolService.prepareOrder(documentCode(request));
    }

    @PostMapping("/receipts/prepare")
    public AgentToolResult<PurchaseApprovalDraft> prepareReceipt(
            @RequestBody(required = false) Map<String, String> request) {
        return toolService.prepareReceipt(documentCode(request));
    }

    @PostMapping("/inbounds/prepare")
    public AgentToolResult<PurchaseApprovalDraft> prepareInbound(
            @RequestBody(required = false) Map<String, String> request) {
        return toolService.prepareInbound(documentCode(request));
    }

    private String documentCode(Map<String, String> request) {
        return request == null ? null : request.get("documentCode");
    }
}
