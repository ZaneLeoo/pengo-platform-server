package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryToolPage;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;

/** 面向 AI 助手的库存只读查询服务。 */
public interface IInventoryAssistantQueryService {
    InventoryToolPage<InventoryBalanceToolItem> queryBalances(InventoryBalanceQuery query);

    InventoryToolPage<InventoryTransactionToolItem> queryTransactions(InventoryTransactionQuery query);
}
