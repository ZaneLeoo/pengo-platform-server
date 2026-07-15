package com.ruoyi.agent.business.automation.purchaseflow.application;

import com.ruoyi.agent.business.automation.domain.AutomationAction;
import com.ruoyi.agent.business.automation.mapper.AutomationActionMapper;
import com.ruoyi.agent.business.automation.purchaseflow.domain.AutomationDocumentResult;
import com.ruoyi.agent.business.automation.purchaseflow.domain.DocumentActionRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.PurchaseApprovalDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.PurchaseApprovalLine;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import com.ruoyi.mes.purchase.mapper.PurchaseInboundMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
import com.ruoyi.mes.purchase.service.ShelfLifeService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购单据审核准备与用户确认执行服务。 */
@Service
public class PurchaseApprovalAutomationService {
    private static final String DRAFT = PurchaseDocumentStatus.DRAFT.getCode();
    private static final String COMPLETED = "COMPLETED";
    private static final String PROCESSING = "PROCESSING";
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseInboundMapper inboundMapper;
    private final IPurchaseFlowService flowService;
    private final ShelfLifeService shelfLifeService;
    private final AutomationActionMapper actionMapper;

    public PurchaseApprovalAutomationService(PurchaseOrderMapper orderMapper, PurchaseReceiptMapper receiptMapper,
            PurchaseInboundMapper inboundMapper, IPurchaseFlowService flowService, ShelfLifeService shelfLifeService,
            AutomationActionMapper actionMapper) {
        this.orderMapper = orderMapper;
        this.receiptMapper = receiptMapper;
        this.inboundMapper = inboundMapper;
        this.flowService = flowService;
        this.shelfLifeService = shelfLifeService;
        this.actionMapper = actionMapper;
    }

    /** 根据采购订单编号准备审核确认数据。 */
    public PurchaseApprovalDraft prepareOrder(String documentCode) {
        PurchaseOrder order = orderMapper.selectPurchaseOrderByCode(requiredCode(documentCode));
        if (order == null) throw new ServiceException("采购订单不存在：" + documentCode);
        requireDraft(order.getStatus(), "采购订单");
        List<PurchaseOrderLine> lines = orderMapper.selectPurchaseOrderLineList(order.getId());
        if (lines.isEmpty()) throw new ServiceException("采购订单没有明细，不能审核");
        PurchaseApprovalDraft draft = base("PURCHASE_ORDER_APPROVE", order.getId(), order.getOrderCode(),
                order.getStatus(), order.getSupplierCode(), order.getSupplierName(), order.getOrderDate(),
                order.getTotalQuantity(), "审核后采购订单将进入可到货状态。");
        draft.setLines(lines.stream().map(this::orderLine).toList());
        return draft;
    }

    /** 根据到货单编号准备审核确认数据。 */
    public PurchaseApprovalDraft prepareReceipt(String documentCode) {
        PurchaseReceipt query = new PurchaseReceipt();
        query.setReceiptCode(requiredCode(documentCode));
        PurchaseReceipt receipt = uniqueReceipt(documentCode, receiptMapper.selectList(query));
        requireDraft(receipt.getStatus(), "采购到货单");
        List<PurchaseReceiptLine> lines = receiptMapper.selectLines(receipt.getId());
        if (lines.isEmpty()) throw new ServiceException("采购到货单没有明细，不能审核");
        lines.forEach(shelfLifeService::validateReceiptLine);
        PurchaseApprovalDraft draft = base("PURCHASE_RECEIPT_APPROVE", receipt.getId(), receipt.getReceiptCode(),
                receipt.getStatus(), receipt.getSupplierCode(), receipt.getSupplierName(), receipt.getReceiptDate(),
                receipt.getTotalQuantity(), "审核后将反写采购订单已到货数量，并允许后续质检。");
        draft.setLines(lines.stream().map(this::receiptLine).toList());
        return draft;
    }

    /** 根据入库单编号准备审核确认数据。 */
    public PurchaseApprovalDraft prepareInbound(String documentCode) {
        PurchaseInbound query = new PurchaseInbound();
        query.setInboundCode(requiredCode(documentCode));
        List<PurchaseInbound> matched = inboundMapper.selectList(query).stream()
                .filter(item -> documentCode.equals(item.getInboundCode())).toList();
        if (matched.size() != 1) throw new ServiceException(matched.isEmpty()
                ? "采购入库单不存在：" + documentCode : "采购入库单编号不唯一：" + documentCode);
        PurchaseInbound inbound = matched.get(0);
        requireDraft(inbound.getStatus(), "采购入库单");
        List<PurchaseInboundLine> lines = inboundMapper.selectLines(inbound.getId());
        if (lines.isEmpty()) throw new ServiceException("采购入库单没有明细，不能审核");
        PurchaseApprovalDraft draft = base("PURCHASE_INBOUND_APPROVE", inbound.getId(), inbound.getInboundCode(),
                inbound.getStatus(), null, null, inbound.getInboundDate(), inbound.getTotalQuantity(),
                "审核后将增加库存余额并生成库存流水，请确认仓库、库位、批次和数量。");
        draft.setLines(lines.stream().map(this::inboundLine).toList());
        return draft;
    }

    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult approveOrder(DocumentActionRequest request, Long userId, String username) {
        return execute(request, userId, "PURCHASE_ORDER_APPROVE", "OA", () -> {
            flowService.approveOrder(request.getDocumentId(), username);
            PurchaseOrder order = orderMapper.selectPurchaseOrderById(request.getDocumentId());
            return result(order.getId(), order.getOrderCode(), order.getStatus());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult approveReceipt(DocumentActionRequest request, Long userId, String username) {
        return execute(request, userId, "PURCHASE_RECEIPT_APPROVE", "RA", () -> {
            flowService.approveReceipt(request.getDocumentId(), username);
            PurchaseReceipt receipt = receiptMapper.selectById(request.getDocumentId());
            return result(receipt.getId(), receipt.getReceiptCode(), receipt.getStatus());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult approveInbound(DocumentActionRequest request, Long userId, String username) {
        return execute(request, userId, "PURCHASE_INBOUND_APPROVE", "IA", () -> {
            flowService.approveInbound(request.getDocumentId(), username);
            PurchaseInbound inbound = inboundMapper.selectById(request.getDocumentId());
            return result(inbound.getId(), inbound.getInboundCode(), inbound.getStatus());
        });
    }

    private AutomationDocumentResult execute(DocumentActionRequest request, Long userId, String type, String prefix,
            Action action) {
        if (request == null || StringUtils.isBlank(request.getRequestId()) || request.getDocumentId() == null)
            throw new ServiceException("确认审核时缺少 requestId 或 documentId");
        String key = prefix + ':' + request.getRequestId().trim();
        synchronized ((type + ':' + key).intern()) {
            AutomationAction existed = actionMapper.selectByActionKey(key);
            if (existed != null) {
                if (!userId.equals(existed.getUserId())) throw new ServiceException("该审核请求不属于当前用户");
                if (COMPLETED.equals(existed.getStatus()))
                    return new AutomationDocumentResult(existed.getTargetId(), existed.getTargetCode(), COMPLETED, true);
                throw new ServiceException("该审核动作正在执行，请勿重复提交");
            }
            AutomationAction record = new AutomationAction();
            record.setActionKey(key);
            record.setActionType(type);
            record.setUserId(userId);
            record.setStatus(PROCESSING);
            actionMapper.insert(record);
            AutomationDocumentResult result = action.execute();
            actionMapper.complete(key, result.getDocumentId(), result.getDocumentCode());
            return result;
        }
    }

    private PurchaseApprovalDraft base(String actionType, Long id, String code, String status, String supplierCode,
            String supplierName, String date, BigDecimal total, String impact) {
        PurchaseApprovalDraft draft = new PurchaseApprovalDraft();
        draft.setActionType(actionType); draft.setDocumentId(id); draft.setDocumentCode(code);
        draft.setDocumentStatus(status); draft.setSupplierCode(supplierCode); draft.setSupplierName(supplierName);
        draft.setDocumentDate(date); draft.setTotalQuantity(total); draft.setImpactMessage(impact);
        return draft;
    }

    private PurchaseApprovalLine orderLine(PurchaseOrderLine source) {
        PurchaseApprovalLine line = common(source.getLineNo(), source.getMaterialCode(), source.getMaterialName(),
                source.getSpec(), source.getUnit(), source.getOrderQuantity());
        return line;
    }

    private PurchaseApprovalLine receiptLine(PurchaseReceiptLine source) {
        PurchaseApprovalLine line = common(source.getLineNo(), source.getMaterialCode(), source.getMaterialName(),
                source.getSpec(), source.getUnit(), source.getReceivedQuantity());
        line.setSourceOrderCode(source.getSourceOrderCode()); line.setWarehouseCode(source.getWarehouseCode());
        line.setWarehouseName(source.getWarehouseName()); line.setLocationCode(source.getLocationCode());
        line.setLocationName(source.getLocationName()); line.setLotNo(source.getLotNo()); return line;
    }

    private PurchaseApprovalLine inboundLine(PurchaseInboundLine source) {
        PurchaseApprovalLine line = common(source.getLineNo(), source.getMaterialCode(), source.getMaterialName(),
                source.getSpec(), source.getUnit(), source.getInboundQuantity());
        line.setSourceOrderCode(source.getSourceOrderCode()); line.setSourceReceiptCode(source.getSourceReceiptCode());
        line.setWarehouseCode(source.getWarehouseCode()); line.setWarehouseName(source.getWarehouseName());
        line.setLocationCode(source.getLocationCode()); line.setLocationName(source.getLocationName());
        line.setLotNo(source.getLotNo()); return line;
    }

    private PurchaseApprovalLine common(Integer lineNo, String materialCode, String materialName, String spec,
            String unit, BigDecimal quantity) {
        PurchaseApprovalLine line = new PurchaseApprovalLine(); line.setLineNo(lineNo); line.setMaterialCode(materialCode);
        line.setMaterialName(materialName); line.setSpec(spec); line.setUnit(unit); line.setQuantity(quantity); return line;
    }

    private PurchaseReceipt uniqueReceipt(String code, List<PurchaseReceipt> values) {
        List<PurchaseReceipt> matched = values.stream().filter(item -> code.equals(item.getReceiptCode())).toList();
        if (matched.size() != 1) throw new ServiceException(matched.isEmpty()
                ? "采购到货单不存在：" + code : "采购到货单编号不唯一：" + code);
        return matched.get(0);
    }

    private String requiredCode(String code) {
        if (StringUtils.isBlank(code)) throw new ServiceException("缺少单据编号 documentCode");
        return code.trim();
    }

    private void requireDraft(String status, String name) {
        if (!DRAFT.equals(status)) throw new ServiceException(name + "当前不是草稿状态，不能审核");
    }

    private AutomationDocumentResult result(Long id, String code, String status) {
        return new AutomationDocumentResult(id, code, status, false);
    }

    @FunctionalInterface
    private interface Action { AutomationDocumentResult execute(); }
}
