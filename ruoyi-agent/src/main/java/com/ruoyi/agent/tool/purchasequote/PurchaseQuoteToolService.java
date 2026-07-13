package com.ruoyi.agent.tool.purchasequote;

import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareResult;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 将采购报价比较业务结果翻译为统一 Agent 工具语义。 */
@Service
public class PurchaseQuoteToolService {
    private final IPurchaseSupplierQuoteService quoteService;

    public PurchaseQuoteToolService(IPurchaseSupplierQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /** 比较当前有效报价，不执行采购写操作。 */
    public AgentToolResult<PurchaseQuoteCompareResult> compare(PurchaseQuoteCompareRequest request) {
        PurchaseQuoteCompareResult result = quoteService.compare(request);
        PurchaseQuoteToolResultCode code = new PurchaseQuoteToolResultCode(result.resultCode());
        List<AgentToolIssue> issues = result.issues().stream()
                .map(field -> AgentToolIssue.of(result.resultCode(), field, result.message(), field))
                .toList();
        return switch (result.status()) {
            case "READY" -> AgentToolResults.present(code, result.message(), result, null,
                    "请展示推荐供应商、可比单价、报价有效期、交货周期和其他候选项；"
                            + "不要直接创建采购订单，等待用户确认后再调用采购订单准备工具。");
            case "NEED_INPUT" -> AgentToolResults.needInput(code, result.message(), issues, result);
            case "NO_CANDIDATE" -> AgentToolResults.noResult(code, result.message(), result, null);
            default -> AgentToolResults.rejected(code, result.message(), issues, result);
        };
    }
}
