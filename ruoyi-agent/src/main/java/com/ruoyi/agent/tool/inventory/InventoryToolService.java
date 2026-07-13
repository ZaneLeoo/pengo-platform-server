package com.ruoyi.agent.tool.inventory;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryToolPage;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;
import com.ruoyi.mes.purchase.service.IInventoryAssistantQueryService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 将库存查询服务适配为统一 Agent 工具协议。 */
@Service
public class InventoryToolService
{
    private final IInventoryAssistantQueryService inventoryQueryService;

    public InventoryToolService(IInventoryAssistantQueryService inventoryQueryService)
    {
        this.inventoryQueryService = inventoryQueryService;
    }

    /** 查询库存余额。 */
    public AgentToolResult<List<InventoryBalanceToolItem>> queryBalances(InventoryBalanceQuery request)
    {
        InventoryBalanceQuery query = request == null
            ? new InventoryBalanceQuery(null, null, null, null, null, null) : request;
        return pageResult(inventoryQueryService.queryBalances(query),
            InventoryToolResultCode.INVENTORY_BALANCE_QUERY_SUCCESS,
            InventoryToolResultCode.INVENTORY_BALANCE_NOT_FOUND, "库存余额");
    }

    /** 查询库存变动流水。 */
    public AgentToolResult<List<InventoryTransactionToolItem>> queryTransactions(InventoryTransactionQuery request)
    {
        InventoryTransactionQuery query = request == null
            ? new InventoryTransactionQuery(null, null, null, null, null, null, null, null, null) : request;
        return pageResult(inventoryQueryService.queryTransactions(query),
            InventoryToolResultCode.INVENTORY_TRANSACTION_QUERY_SUCCESS,
            InventoryToolResultCode.INVENTORY_TRANSACTION_NOT_FOUND, "库存流水");
    }

    /** 统一分页结果和无数据语义。 */
    private <T> AgentToolResult<List<T>> pageResult(InventoryToolPage<T> page,
                                                     InventoryToolResultCode successCode,
                                                     InventoryToolResultCode emptyCode,
                                                     String subject)
    {
        AgentToolMeta meta = AgentToolMeta.page(page.pageNum(), page.pageSize(), page.total(), page.hasMore());
        if (page.data().isEmpty())
        {
            return AgentToolResults.noResult(emptyCode, "未找到符合条件的" + subject,
                page.data(), meta);
        }
        return AgentToolResults.success(successCode, subject + "查询成功", page.data(), meta);
    }
}
