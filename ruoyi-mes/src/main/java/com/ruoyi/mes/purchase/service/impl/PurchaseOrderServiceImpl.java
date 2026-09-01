package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购订单业务处理。 */
@Service
public class PurchaseOrderServiceImpl implements IPurchaseOrderService {
    private final PurchaseOrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PurchaseOrderServiceImpl(
            PurchaseOrderMapper orderMapper, ApplicationEventPublisher eventPublisher) {
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<PurchaseOrder> selectPurchaseOrderList(PurchaseOrder order) {
        return orderMapper.selectPurchaseOrderList(order);
    }

    @Override
    public PurchaseOrder selectPurchaseOrderById(Long id) {
        PurchaseOrder order = orderMapper.selectPurchaseOrderById(id);
        if (order != null) order.setLines(orderMapper.selectPurchaseOrderLineList(id));
        return order;
    }

    @Override
    public boolean checkOrderCodeUnique(PurchaseOrder order) {
        PurchaseOrder existing = orderMapper.selectPurchaseOrderByCode(order.getOrderCode());
        return existing == null || existing.getId().equals(order.getId());
    }

    @Override
    @Transactional
    public int insertPurchaseOrder(PurchaseOrder order) {
        prepareOrder(order);
        eventPublisher.publishEvent(
                new com.ruoyi.mes.purchase.event.PurchaseOrderProjectValidationEvent(order));
        orderMapper.insertPurchaseOrder(order);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int updatePurchaseOrder(PurchaseOrder order) {
        prepareOrder(order);
        eventPublisher.publishEvent(
                new com.ruoyi.mes.purchase.event.PurchaseOrderProjectValidationEvent(order));
        orderMapper.updatePurchaseOrder(order);
        Long[] ids = {order.getId()};
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int deletePurchaseOrderByIds(Long[] ids) {
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        return orderMapper.deletePurchaseOrderByIds(ids);
    }

    private void insertLines(PurchaseOrder order) {
        if (order.getLines() == null) return;
        for (PurchaseOrderLine line : order.getLines()) {
            line.setOrderId(order.getId());
            if (line.getLineNo() == null) line.setLineNo(order.getLines().indexOf(line) + 1);
            if (line.getReceivedQuantity() == null)
                line.setReceivedQuantity(java.math.BigDecimal.ZERO);
            if (line.getQualifiedQuantity() == null)
                line.setQualifiedQuantity(java.math.BigDecimal.ZERO);
            if (line.getInboundQuantity() == null)
                line.setInboundQuantity(java.math.BigDecimal.ZERO);
            if (com.ruoyi.common.utils.StringUtils.isBlank(line.getPriceSource()))
                line.setPriceSource("MANUAL");
            orderMapper.insertPurchaseOrderLine(line);
        }
    }

    /**
     * 重新计算订单行并汇总订单金额。
     *
     * <p>采购行按物料的唯一计量单位计价。
     */
    private void prepareOrder(PurchaseOrder order) {
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new IllegalArgumentException("采购订单至少需要一条明细");
        }
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderLine line : order.getLines()) {
            normalizeLine(line);
            BigDecimal unitPrice =
                    line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("采购单价不能小于0: " + line.getMaterialCode());
            }
            line.setUnitPrice(unitPrice);
            BigDecimal amount =
                    line.getOrderQuantity().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            line.setAmount(amount);
            totalQuantity = totalQuantity.add(line.getOrderQuantity());
            totalAmount = totalAmount.add(amount);
        }
        order.setTotalQuantity(scale(totalQuantity, 6));
        order.setTotalAmount(scale(totalAmount, 2));
    }

    private void normalizeLine(PurchaseOrderLine line) {
        if (com.ruoyi.common.utils.StringUtils.isBlank(line.getUnit())) {
            throw new IllegalArgumentException("采购明细计量单位不能为空: " + line.getMaterialCode());
        }
        if (line.getOrderQuantity() == null
                || line.getOrderQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("采购明细数量必须大于0: " + line.getMaterialCode());
        }
        line.setOrderQuantity(scale(line.getOrderQuantity(), 6));
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
