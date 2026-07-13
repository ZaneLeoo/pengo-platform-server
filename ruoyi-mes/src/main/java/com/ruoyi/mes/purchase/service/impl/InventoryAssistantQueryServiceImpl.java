package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryToolPage;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;
import com.ruoyi.mes.purchase.mapper.InventoryAgentQueryMapper;
import com.ruoyi.mes.purchase.service.IInventoryAssistantQueryService;
import java.util.List;
import org.springframework.stereotype.Service;

/** AI 库存查询服务实现。 */
@Service
public class InventoryAssistantQueryServiceImpl implements IInventoryAssistantQueryService {
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final InventoryAgentQueryMapper inventoryQueryMapper;

    public InventoryAssistantQueryServiceImpl(InventoryAgentQueryMapper inventoryQueryMapper) {
        this.inventoryQueryMapper = inventoryQueryMapper;
    }

    /** 查询指定条件下的当前库存余额。 */
    @Override
    public InventoryToolPage<InventoryBalanceToolItem> queryBalances(InventoryBalanceQuery query) {
        int pageNum = resolvePageNum(query.pageNum());
        int pageSize = resolvePageSize(query.pageSize());
        long total = inventoryQueryMapper.countBalances(query);
        List<InventoryBalanceToolItem> data = total == 0
                ? List.of()
                : inventoryQueryMapper.selectBalanceList(query, offset(pageNum, pageSize), pageSize);
        return page(data, pageNum, pageSize, total);
    }

    /** 查询指定条件下的库存变动流水。 */
    @Override
    public InventoryToolPage<InventoryTransactionToolItem> queryTransactions(InventoryTransactionQuery query) {
        validateDateRange(query);
        int pageNum = resolvePageNum(query.pageNum());
        int pageSize = resolvePageSize(query.pageSize());
        long total = inventoryQueryMapper.countTransactions(query);
        List<InventoryTransactionToolItem> data = total == 0
                ? List.of()
                : inventoryQueryMapper.selectTransactionList(query, offset(pageNum, pageSize), pageSize);
        return page(data, pageNum, pageSize, total);
    }

    /** 校验流水查询日期范围。 */
    private void validateDateRange(InventoryTransactionQuery query) {
        if (query.beginDate() != null && query.endDate() != null && query.beginDate().isAfter(query.endDate())) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
    }

    /** 规范页码。 */
    private int resolvePageNum(Integer pageNum) {
        return pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
    }

    /** 规范每页数量并保护模型上下文。 */
    private int resolvePageSize(Integer pageSize) {
        return pageSize == null ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /** 计算 SQL 分页偏移量。 */
    private int offset(int pageNum, int pageSize) {
        return (pageNum - 1) * pageSize;
    }

    /** 组装统一的分页结果。 */
    private <T> InventoryToolPage<T> page(List<T> data, int pageNum, int pageSize, long total) {
        return new InventoryToolPage<>(data, pageNum, pageSize, total, (long) pageNum * pageSize < total);
    }
}
