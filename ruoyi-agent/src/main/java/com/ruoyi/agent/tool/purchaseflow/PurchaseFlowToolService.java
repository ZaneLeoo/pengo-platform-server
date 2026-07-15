package com.ruoyi.agent.tool.purchaseflow;

import com.ruoyi.agent.business.automation.purchaseflow.application.PurchaseFlowAutomationService;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraftRequest;
import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import java.util.List;
import org.springframework.stereotype.Service;

/** Dify 采购闭环只读工具适配层。 */
@Service
public class PurchaseFlowToolService {
    private final PurchaseFlowAutomationService automationService;

    public PurchaseFlowToolService(PurchaseFlowAutomationService automationService) {
        this.automationService = automationService;
    }

    /** 查询可生成到货单的采购订单行。 */
    public AgentToolResult<List<ReceiptReferenceLine>> receiptCandidates(String orderCode, String supplierName,
            String materialCode) {
        List<ReceiptReferenceLine> data = automationService.receiptCandidates(orderCode, supplierName, materialCode);
        AgentToolMeta meta = AgentToolMeta.collection(data.size(), false);
        return data.isEmpty()
                ? AgentToolResults.noResult(PurchaseFlowToolResultCode.RECEIPT_CANDIDATES_FOUND,
                        "没有查询到可到货的采购订单明细", data, meta)
                : AgentToolResults.success(PurchaseFlowToolResultCode.RECEIPT_CANDIDATES_FOUND,
                        "已查询到 " + data.size() + " 条可到货采购订单明细", data, meta);
    }

    /** 准备到货草稿，真实创建必须由前端确认。 */
    public AgentToolResult<ReceiptDraft> prepareReceipt(ReceiptDraftRequest request) {
        try {
            ReceiptDraft draft = automationService.prepareReceipt(request);
            return AgentToolResults.confirm(PurchaseFlowToolResultCode.RECEIPT_DRAFT_READY,
                    "采购到货草稿已准备", draft, "由 agent-ui 展示到货确认卡片并等待用户确认。 ");
        } catch (ServiceException exception) {
            return rejected(exception);
        }
    }

    /** 准备质检草稿，真实质检必须由前端确认。 */
    public AgentToolResult<InspectionDraft> prepareInspection(InspectionDraftRequest request) {
        try {
            InspectionDraft draft = automationService.prepareInspection(request);
            return AgentToolResults.confirm(PurchaseFlowToolResultCode.INSPECTION_DRAFT_READY,
                    "采购到货质检结果已准备", draft, "由 agent-ui 展示质检确认卡片并等待用户确认。 ");
        } catch (ServiceException exception) {
            return rejected(exception);
        }
    }

    /** 查询可生成入库单的已质检到货行。 */
    public AgentToolResult<List<InboundReferenceLine>> inboundCandidates(String receiptCode, String warehouseCode,
            String materialCode) {
        List<InboundReferenceLine> data = automationService.inboundCandidates(receiptCode, warehouseCode, materialCode);
        AgentToolMeta meta = AgentToolMeta.collection(data.size(), false);
        return data.isEmpty()
                ? AgentToolResults.noResult(PurchaseFlowToolResultCode.INBOUND_CANDIDATES_FOUND,
                        "没有查询到可入库的已质检到货明细", data, meta)
                : AgentToolResults.success(PurchaseFlowToolResultCode.INBOUND_CANDIDATES_FOUND,
                        "已查询到 " + data.size() + " 条可入库到货明细", data, meta);
    }

    /** 准备入库草稿，真实创建必须由前端确认。 */
    public AgentToolResult<InboundDraft> prepareInbound(InboundDraftRequest request) {
        try {
            InboundDraft draft = automationService.prepareInbound(request);
            return AgentToolResults.confirm(PurchaseFlowToolResultCode.INBOUND_DRAFT_READY,
                    "采购入库草稿已准备", draft, "由 agent-ui 展示入库确认卡片并等待用户确认。 ");
        } catch (ServiceException exception) {
            return rejected(exception);
        }
    }

    private <T> AgentToolResult<T> rejected(ServiceException exception) {
        AgentToolIssue issue = AgentToolIssue.of("BUSINESS_VALIDATION_FAILED", "", exception.getMessage(),
                "根据错误信息补充或修正字段");
        return AgentToolResults.rejected(PurchaseFlowToolResultCode.PURCHASE_FLOW_VALIDATION_FAILED,
                exception.getMessage(), List.of(issue), null);
    }
}
