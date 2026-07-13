package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** AI 报价比较工具的结构化结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteCompareResult {
    private String status;
    private String resultCode;
    private String nextAction;
    private String message;
    private List<String> issues;
    private List<PurchaseQuoteRecommendation> recommendations;
    private List<PurchaseQuoteCandidate> candidates;

}
