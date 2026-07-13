package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** AI 比较当前有效供应商报价的请求。字段名与 Dify OpenAPI 保持一致。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteCompareRequest {
    private List<PurchaseQuoteCompareLineRequest> lines;
    private String currency;
    private String priceBasis;
    private String strategy;

}
