package com.ruoyi.agent.business.automation.purchaseflow.domain;

import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 等待用户确认的采购入库草稿。 */
@Data
public class InboundDraft {
    private String actionType = "PURCHASE_INBOUND_CREATE";
    private String inboundDate;
    private String remark;
    private BigDecimal totalQuantity;
    private List<PurchaseInboundLine> lines;
}
