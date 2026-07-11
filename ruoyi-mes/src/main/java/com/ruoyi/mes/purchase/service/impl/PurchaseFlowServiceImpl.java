package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.common.enums.InventoryTransactionType;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.common.enums.ReceiptInspectionStatus;
import com.ruoyi.mes.purchase.domain.InventoryTransaction;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.InspectionLineRequest;
import com.ruoyi.mes.purchase.domain.dto.InspectionRequest;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import com.ruoyi.mes.purchase.mapper.PurchaseFlowMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseInboundMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购、送货、入库单据流转实现。 */
@Service
public class PurchaseFlowServiceImpl implements IPurchaseFlowService {
    private static final String DRAFT = PurchaseDocumentStatus.DRAFT.getCode();
    private static final String APPROVED = PurchaseDocumentStatus.APPROVED.getCode();
    private static final String PENDING = ReceiptInspectionStatus.PENDING.getCode();
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseInboundMapper inboundMapper;
    private final PurchaseFlowMapper flowMapper;

    public PurchaseFlowServiceImpl(PurchaseOrderMapper orderMapper, PurchaseReceiptMapper receiptMapper,
                                   PurchaseInboundMapper inboundMapper, PurchaseFlowMapper flowMapper) {
        this.orderMapper = orderMapper;
        this.receiptMapper = receiptMapper;
        this.inboundMapper = inboundMapper;
        this.flowMapper = flowMapper;
    }

    @Override @Transactional
    public void approveOrder(Long id, String operator) {
        PurchaseOrder order = requiredOrder(id);
        assertStatus(order.getStatus(), DRAFT, "采购订单只有草稿状态可以审核");
        if (flowMapper.updateOrderStatus(id, DRAFT, APPROVED, operator) != 1) throw new ServiceException("采购订单状态已变化，请刷新后重试");
    }

    @Override @Transactional
    public void unapproveOrder(Long id, String operator) {
        PurchaseOrder order = requiredOrder(id);
        assertStatus(order.getStatus(), APPROVED, "采购订单只有已审核状态可以弃审");
        if (flowMapper.countApprovedReceiptByOrder(id) > 0) throw new ServiceException("采购订单已生成已审核送货单，不能弃审");
        if (flowMapper.updateOrderStatus(id, APPROVED, DRAFT, operator) != 1) throw new ServiceException("采购订单状态已变化，请刷新后重试");
    }

    @Override @Transactional
    public void approveReceipt(Long id, String operator) {
        PurchaseReceipt receipt = requiredReceipt(id);
        assertStatus(receipt.getStatus(), DRAFT, "送货单只有草稿状态可以审核");
        List<PurchaseReceiptLine> lines = receiptMapper.selectLines(id);
        if (lines.isEmpty()) throw new ServiceException("送货单至少需要一条明细");
        if (flowMapper.countReceiptSourceSuppliers(id) > 1) throw new ServiceException("一张送货单只能参照同一供应商的采购订单明细");
        if (flowMapper.countReceiptHeaderSupplierMismatch(id) > 0) throw new ServiceException("送货单供应商必须与来源采购订单一致");
        for (PurchaseReceiptLine line : lines) {
            if (flowMapper.countValidReceiptSource(line) != 1) throw new ServiceException("物料 " + line.getMaterialCode() + " 的来源采购订单明细无效");
            if (flowMapper.increaseOrderReceived(line.getSourceOrderLineId(), line.getReceivedQuantity()) != 1) {
                throw new ServiceException("物料 " + line.getMaterialCode() + " 的到货数量超过订单剩余数量，或来源订单未审核");
            }
        }
        flowMapper.resetReceiptInspection(id);
        if (flowMapper.updateReceiptStatus(id, DRAFT, APPROVED, operator) != 1) throw new ServiceException("送货单状态已变化，请刷新后重试");
    }

    @Override @Transactional
    public void unapproveReceipt(Long id, String operator) {
        PurchaseReceipt receipt = requiredReceipt(id);
        assertStatus(receipt.getStatus(), APPROVED, "送货单只有已审核状态可以弃审");
        if (!PENDING.equals(receipt.getInspectionStatus())) throw new ServiceException("送货单已完成质检，请先反质检后再弃审");
        if (flowMapper.countApprovedInboundByReceipt(id) > 0) throw new ServiceException("送货单已生成已审核入库单，不能弃审");
        for (PurchaseReceiptLine line : receiptMapper.selectLines(id)) {
            if (flowMapper.increaseOrderReceived(line.getSourceOrderLineId(), line.getReceivedQuantity().negate()) != 1) throw new ServiceException("订单到货数量反写失败");
        }
        if (flowMapper.updateReceiptStatus(id, APPROVED, DRAFT, operator) != 1) throw new ServiceException("送货单状态已变化，请刷新后重试");
    }

    @Override @Transactional
    public void inspectReceipt(Long id, InspectionRequest request, String operator) {
        PurchaseReceipt receipt = requiredReceipt(id);
        assertStatus(receipt.getStatus(), APPROVED, "送货单审核后才可以质检");
        if (!PENDING.equals(receipt.getInspectionStatus())) throw new ServiceException("送货单当前不是待检状态");
        List<PurchaseReceiptLine> lines = receiptMapper.selectLines(id);
        Map<Long, InspectionLineRequest> results = new HashMap<>();
        for (InspectionLineRequest result : request.getLines()) {
            if (results.put(result.getReceiptLineId(), result) != null) throw new ServiceException("质检明细不能重复");
        }
        if (results.size() != lines.size()) throw new ServiceException("请填写全部送货明细的质检结果");
        BigDecimal totalQualified = BigDecimal.ZERO;
        BigDecimal totalRejected = BigDecimal.ZERO;
        for (PurchaseReceiptLine line : lines) {
            InspectionLineRequest result = results.get(line.getId());
            if (result == null || result.getQualifiedQuantity().add(result.getRejectedQuantity()).compareTo(line.getReceivedQuantity()) != 0) {
                throw new ServiceException("物料 " + line.getMaterialCode() + " 的合格数与不合格数之和必须等于到货数量");
            }
            line.setQualifiedQuantity(result.getQualifiedQuantity());
            line.setRejectedQuantity(result.getRejectedQuantity());
            line.setPendingQuantity(BigDecimal.ZERO);
            line.setUpdateBy(operator);
            if (flowMapper.updateReceiptInspection(line) != 1 || flowMapper.increaseOrderQualified(line.getSourceOrderLineId(), result.getQualifiedQuantity()) != 1) {
                throw new ServiceException("物料 " + line.getMaterialCode() + " 的合格数量反写失败");
            }
            totalQualified = totalQualified.add(result.getQualifiedQuantity());
            totalRejected = totalRejected.add(result.getRejectedQuantity());
        }
        String inspectionStatus = resolveInspectionStatus(totalQualified, totalRejected);
        flowMapper.updateReceiptInspectionStatus(id, inspectionStatus, operator);
    }

    @Override @Transactional
    public void uninspectReceipt(Long id, String operator) {
        PurchaseReceipt receipt = requiredReceipt(id);
        assertStatus(receipt.getStatus(), APPROVED, "送货单审核后才可以反质检");
        if (PENDING.equals(receipt.getInspectionStatus())) throw new ServiceException("送货单尚未质检");
        if (flowMapper.countApprovedInboundByReceipt(id) > 0) throw new ServiceException("送货单已生成已审核入库单，不能反质检");
        for (PurchaseReceiptLine line : receiptMapper.selectLines(id)) {
            if (flowMapper.increaseOrderQualified(line.getSourceOrderLineId(), line.getQualifiedQuantity().negate()) != 1) throw new ServiceException("订单合格数量反写失败");
        }
        flowMapper.resetReceiptInspection(id);
        flowMapper.updateReceiptInspectionStatus(id, PENDING, operator);
    }

    @Override @Transactional
    public void approveInbound(Long id, String operator) {
        PurchaseInbound inbound = requiredInbound(id);
        assertStatus(inbound.getStatus(), DRAFT, "入库单只有草稿状态可以审核");
        List<PurchaseInboundLine> lines = inboundMapper.selectLines(id);
        if (lines.isEmpty()) throw new ServiceException("入库单至少需要一条明细");
        if (flowMapper.countInboundSourceWarehouses(id) > 1) throw new ServiceException("一张入库单只能包含同一仓库的送货明细");
        if (flowMapper.countInboundHeaderWarehouseMismatch(id) > 0) throw new ServiceException("入库单仓库必须与来源送货明细一致");
        if (flowMapper.updateInboundStatus(id, DRAFT, APPROVED, operator) != 1) throw new ServiceException("入库单状态已变化，请刷新后重试");
        for (PurchaseInboundLine line : lines) {
            if (flowMapper.countValidInboundSource(line) != 1) throw new ServiceException("物料 " + line.getMaterialCode() + " 的来源送货明细无效");
            if (flowMapper.increaseReceiptInbound(line.getSourceReceiptLineId(), line.getInboundQuantity()) != 1 || flowMapper.increaseOrderInbound(line.getSourceOrderLineId(), line.getInboundQuantity()) != 1) {
                throw new ServiceException("物料 " + line.getMaterialCode() + " 的可入库数量不足，或来源送货单未完成质检");
            }
            InventoryTransaction transaction = buildTransaction(inbound, line, line.getInboundQuantity(), InventoryTransactionType.INBOUND.getCode(), operator);
            flowMapper.upsertInventoryBalance(transaction);
            flowMapper.insertInventoryTransaction(transaction);
        }
    }

    @Override @Transactional
    public void unapproveInbound(Long id, String operator) {
        PurchaseInbound inbound = requiredInbound(id);
        assertStatus(inbound.getStatus(), APPROVED, "入库单只有已审核状态可以弃审");
        for (PurchaseInboundLine line : inboundMapper.selectLines(id)) {
            if (flowMapper.increaseReceiptInbound(line.getSourceReceiptLineId(), line.getInboundQuantity().negate()) != 1 || flowMapper.increaseOrderInbound(line.getSourceOrderLineId(), line.getInboundQuantity().negate()) != 1) {
                throw new ServiceException("物料 " + line.getMaterialCode() + " 的入库数量反写失败");
            }
            InventoryTransaction transaction = buildTransaction(inbound, line, line.getInboundQuantity(), InventoryTransactionType.INBOUND_REVERSE.getCode(), operator);
            if (flowMapper.decreaseInventoryBalance(transaction) != 1) throw new ServiceException("物料 " + line.getMaterialCode() + " 的库存余额不足，不能弃审");
            transaction.setQuantity(transaction.getQuantity().negate());
            flowMapper.insertInventoryTransaction(transaction);
        }
        if (flowMapper.updateInboundStatus(id, APPROVED, DRAFT, operator) != 1) throw new ServiceException("入库单状态已变化，请刷新后重试");
    }

    @Override public List<ReceiptReferenceLine> selectReceiptReferenceLines(String orderCode, String supplierName) { return flowMapper.selectReceiptReferenceLines(orderCode, supplierName); }
    @Override public List<InboundReferenceLine> selectInboundReferenceLines(String receiptCode, String warehouseCode) { return flowMapper.selectInboundReferenceLines(receiptCode, warehouseCode); }

    private PurchaseOrder requiredOrder(Long id) { PurchaseOrder order = orderMapper.selectPurchaseOrderById(id); if (order == null) throw new ServiceException("采购订单不存在"); return order; }
    private PurchaseReceipt requiredReceipt(Long id) { PurchaseReceipt receipt = receiptMapper.selectById(id); if (receipt == null) throw new ServiceException("送货单不存在"); return receipt; }
    private PurchaseInbound requiredInbound(Long id) { PurchaseInbound inbound = inboundMapper.selectById(id); if (inbound == null) throw new ServiceException("入库单不存在"); return inbound; }
    private void assertStatus(String actual, String expected, String message) { if (!expected.equals(actual)) throw new ServiceException(message); }

    /** 根据合格与不合格总数归纳本次质检状态。 */
    private String resolveInspectionStatus(BigDecimal totalQualified, BigDecimal totalRejected) {
        if (totalRejected.signum() == 0) return ReceiptInspectionStatus.PASSED.getCode();
        if (totalQualified.signum() == 0) return ReceiptInspectionStatus.FAILED.getCode();
        return ReceiptInspectionStatus.PARTIAL.getCode();
    }
    private InventoryTransaction buildTransaction(PurchaseInbound inbound, PurchaseInboundLine line, BigDecimal quantity, String type, String operator) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType(type); transaction.setBusinessId(inbound.getId()); transaction.setBusinessCode(inbound.getInboundCode()); transaction.setBusinessLineId(line.getId());
        transaction.setMaterialId(line.getMaterialId()); transaction.setMaterialCode(line.getMaterialCode()); transaction.setMaterialName(line.getMaterialName());
        transaction.setWarehouseCode(line.getWarehouseCode()); transaction.setLocationCode(line.getLocationCode() == null ? "" : line.getLocationCode()); transaction.setLotNo(line.getLotNo() == null ? "" : line.getLotNo());
        transaction.setUnit(line.getUnit()); transaction.setQuantity(quantity); transaction.setSourceReceiptLineId(line.getSourceReceiptLineId()); transaction.setSourceOrderLineId(line.getSourceOrderLineId()); transaction.setCreateBy(operator);
        return transaction;
    }
}
