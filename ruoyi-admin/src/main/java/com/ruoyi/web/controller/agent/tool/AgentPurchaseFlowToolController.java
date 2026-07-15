package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraftRequest;
import com.ruoyi.agent.tool.purchaseflow.PurchaseFlowToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Dify 采购到货、质检、入库只读工具入口。 */
@RestController
@RequestMapping("/agent/tools/purchase-flow")
public class AgentPurchaseFlowToolController {
    private final PurchaseFlowToolService toolService;

    public AgentPurchaseFlowToolController(PurchaseFlowToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping("/receipt-candidates")
    public AgentToolResult<List<ReceiptReferenceLine>> receiptCandidates(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String materialCode) {
        return toolService.receiptCandidates(orderCode, supplierName, materialCode);
    }

    @PostMapping("/receipts/prepare")
    public AgentToolResult<ReceiptDraft> prepareReceipt(@RequestBody(required = false) ReceiptDraftRequest request) {
        return toolService.prepareReceipt(request);
    }

    @PostMapping("/inspections/prepare")
    public AgentToolResult<InspectionDraft> prepareInspection(
            @RequestBody(required = false) InspectionDraftRequest request) {
        return toolService.prepareInspection(request);
    }

    @GetMapping("/inbound-candidates")
    public AgentToolResult<List<InboundReferenceLine>> inboundCandidates(
            @RequestParam(required = false) String receiptCode,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String materialCode) {
        return toolService.inboundCandidates(receiptCode, warehouseCode, materialCode);
    }

    @PostMapping("/inbounds/prepare")
    public AgentToolResult<InboundDraft> prepareInbound(@RequestBody(required = false) InboundDraftRequest request) {
        return toolService.prepareInbound(request);
    }
}
