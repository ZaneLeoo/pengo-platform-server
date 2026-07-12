package com.ruoyi.agent.business.automation.domain;

import java.math.BigDecimal;

/** AI 准备采购订单草稿的一行输入。 */
public record PurchaseOrderDraftLineRequest(
    String materialKeyword,
    BigDecimal quantity,
    BigDecimal unitPrice,
    String plannedDate)
{
}
