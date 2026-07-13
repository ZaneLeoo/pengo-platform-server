package com.ruoyi.mes.purchase.domain.dto;

import java.util.List;

/** AI 报价比较工具的结构化结果。 */
public record PurchaseQuoteCompareResult(
        String status,
        String resultCode,
        String nextAction,
        String message,
        List<String> issues,
        List<PurchaseQuoteRecommendation> recommendations,
        List<PurchaseQuoteCandidate> candidates) {
}
