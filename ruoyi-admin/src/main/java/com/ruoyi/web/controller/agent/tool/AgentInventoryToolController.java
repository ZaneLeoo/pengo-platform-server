package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.inventory.InventoryToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 库存查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/inventory")
public class AgentInventoryToolController {
    private final InventoryToolService toolService;

    public AgentInventoryToolController(InventoryToolService toolService) {
        this.toolService = toolService;
    }

    /** 查询当前库存余额。 */
    @PostMapping("/balances/query")
    public AgentToolResult<List<InventoryBalanceToolItem>> queryBalances(
            @Valid @RequestBody(required = false) InventoryBalanceQuery request) {
        return toolService.queryBalances(request);
    }

    /** 查询库存变动流水。 */
    @PostMapping("/transactions/query")
    public AgentToolResult<List<InventoryTransactionToolItem>> queryTransactions(
            @Valid @RequestBody(required = false) InventoryTransactionQuery request) {
        return toolService.queryTransactions(request);
    }
}
