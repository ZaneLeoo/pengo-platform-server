package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraft;
import com.ruoyi.agent.tool.purchaseorder.PurchaseOrderToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 采购订单草稿准备工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/purchase-orders")
public class AgentPurchaseOrderToolController {
    private final PurchaseOrderToolService toolService;

    public AgentPurchaseOrderToolController(PurchaseOrderToolService toolService) {
        this.toolService = toolService;
    }

    /** 校验并准备采购订单草稿，不执行写操作。 */
    @PostMapping("/prepare")
    public AgentToolResult<PurchaseOrderDraft> prepare(
            @RequestBody(required = false) PurchaseOrderDraftRequest request) {
        return toolService.prepare(request);
    }
}
