package com.ruoyi.mes.purchase.event;

import com.ruoyi.mes.purchase.domain.PurchaseOrder;

/** 采购订单保存前的项目归集校验事件。 */
public record PurchaseOrderProjectValidationEvent(PurchaseOrder order) {}
