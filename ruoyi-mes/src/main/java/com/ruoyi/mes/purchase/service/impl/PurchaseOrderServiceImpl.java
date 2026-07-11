package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购订单业务处理。 */
@Service
public class PurchaseOrderServiceImpl implements IPurchaseOrderService
{
    private final PurchaseOrderMapper orderMapper;

    public PurchaseOrderServiceImpl(PurchaseOrderMapper orderMapper)
    {
        this.orderMapper = orderMapper;
    }

    @Override
    public List<PurchaseOrder> selectPurchaseOrderList(PurchaseOrder order)
    {
        return orderMapper.selectPurchaseOrderList(order);
    }

    @Override
    public PurchaseOrder selectPurchaseOrderById(Long id)
    {
        PurchaseOrder order = orderMapper.selectPurchaseOrderById(id);
        if (order != null) order.setLines(orderMapper.selectPurchaseOrderLineList(id));
        return order;
    }

    @Override
    public boolean checkOrderCodeUnique(PurchaseOrder order)
    {
        PurchaseOrder existing = orderMapper.selectPurchaseOrderByCode(order.getOrderCode());
        return existing == null || existing.getId().equals(order.getId());
    }

    @Override
    @Transactional
    public int insertPurchaseOrder(PurchaseOrder order)
    {
        orderMapper.insertPurchaseOrder(order);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int updatePurchaseOrder(PurchaseOrder order)
    {
        orderMapper.updatePurchaseOrder(order);
        Long[] ids = {order.getId()};
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int deletePurchaseOrderByIds(Long[] ids)
    {
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        return orderMapper.deletePurchaseOrderByIds(ids);
    }

    private void insertLines(PurchaseOrder order)
    {
        if (order.getLines() == null) return;
        for (PurchaseOrderLine line : order.getLines())
        {
            line.setOrderId(order.getId());
            if (line.getLineNo() == null) line.setLineNo(order.getLines().indexOf(line) + 1);
            if (line.getReceivedQuantity() == null) line.setReceivedQuantity(java.math.BigDecimal.ZERO);
            if (line.getQualifiedQuantity() == null) line.setQualifiedQuantity(java.math.BigDecimal.ZERO);
            orderMapper.insertPurchaseOrderLine(line);
        }
    }
}
