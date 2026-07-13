package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.purchasequote.PurchaseQuoteToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 供应商报价比较工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/purchase-quotes")
public class AgentPurchaseQuoteToolController
{
    private final PurchaseQuoteToolService toolService;

    public AgentPurchaseQuoteToolController(PurchaseQuoteToolService toolService)
    {
        this.toolService = toolService;
    }

    /** 比较有效供应商报价。 */
    @PostMapping("/compare")
    public AgentToolResult<PurchaseQuoteCompareResult> compare(
        @RequestBody(required = false) PurchaseQuoteCompareRequest request)
    {
        return toolService.compare(request);
    }
}
