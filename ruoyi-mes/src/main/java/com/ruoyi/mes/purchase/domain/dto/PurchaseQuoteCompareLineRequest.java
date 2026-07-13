package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;

/** AI 比较报价的物料与数量条件。 */
public record PurchaseQuoteCompareLineRequest(
    String materialCode,
    BigDecimal quantity,
    String requiredDate)
{
}
