package com.ruoyi.mes.purchase.domain.dto;

import java.util.List;

/** AI 比较当前有效供应商报价的请求。字段名与 Dify OpenAPI 保持一致。 */
public record PurchaseQuoteCompareRequest(
        List<PurchaseQuoteCompareLineRequest> lines,
        String currency,
        String priceBasis,
        String strategy) {
}
