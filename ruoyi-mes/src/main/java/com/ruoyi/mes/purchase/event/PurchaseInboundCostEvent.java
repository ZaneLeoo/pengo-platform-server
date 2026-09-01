package com.ruoyi.mes.purchase.event;

import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import java.util.List;

/** 入库审核/弃审时发布的项目成本归集事件，由项目管理模块同步处理。 */
public record PurchaseInboundCostEvent(
        PurchaseInbound inbound,
        List<PurchaseInboundLine> lines,
        String operator,
        boolean reversed) {}
