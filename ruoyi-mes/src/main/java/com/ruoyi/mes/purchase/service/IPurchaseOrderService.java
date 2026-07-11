package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import java.util.List;

/** 采购订单业务接口。 */
public interface IPurchaseOrderService
{
    List<PurchaseOrder> selectPurchaseOrderList(PurchaseOrder order);
    PurchaseOrder selectPurchaseOrderById(Long id);
    boolean checkOrderCodeUnique(PurchaseOrder order);
    int insertPurchaseOrder(PurchaseOrder order);
    int updatePurchaseOrder(PurchaseOrder order);
    int deletePurchaseOrderByIds(Long[] ids);
}
