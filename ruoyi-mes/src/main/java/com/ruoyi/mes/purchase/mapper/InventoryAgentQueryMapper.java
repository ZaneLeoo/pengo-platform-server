package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 面向 AI 的库存只读查询数据访问层。 */
public interface InventoryAgentQueryMapper {
    List<InventoryBalanceToolItem> selectBalanceList(@Param("query") InventoryBalanceQuery query,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    long countBalances(@Param("query") InventoryBalanceQuery query);

    List<InventoryTransactionToolItem> selectTransactionList(@Param("query") InventoryTransactionQuery query,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    long countTransactions(@Param("query") InventoryTransactionQuery query);
}
