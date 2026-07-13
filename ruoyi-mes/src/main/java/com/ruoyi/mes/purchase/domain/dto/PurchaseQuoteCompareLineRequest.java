package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** AI 比较报价的物料与数量条件。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteCompareLineRequest {
    private String materialCode;
    private BigDecimal quantity;
    private String requiredDate;

}
