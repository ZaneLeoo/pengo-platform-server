package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import java.util.List;

/** 采购订单数据访问接口。 */
public interface PurchaseOrderMapper
{
    List<PurchaseOrder> selectPurchaseOrderList(PurchaseOrder order);
    PurchaseOrder selectPurchaseOrderById(Long id);
    PurchaseOrder selectPurchaseOrderByCode(String orderCode);
    int insertPurchaseOrder(PurchaseOrder order);
    int updatePurchaseOrder(PurchaseOrder order);
    int deletePurchaseOrderByIds(Long[] ids);
    int deletePurchaseOrderLineByOrderIds(Long[] ids);
    List<PurchaseOrderLine> selectPurchaseOrderLineList(Long orderId);
    int insertPurchaseOrderLine(PurchaseOrderLine line);
    int updatePurchaseOrderLine(PurchaseOrderLine line);
    int deletePurchaseOrderLineByIds(Long[] ids);
}
