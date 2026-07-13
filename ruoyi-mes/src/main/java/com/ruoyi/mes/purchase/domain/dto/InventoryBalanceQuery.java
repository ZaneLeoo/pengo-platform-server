package com.ruoyi.mes.purchase.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** AI 查询库存余额的筛选条件。 */
public record InventoryBalanceQuery(
        @Size(max = 100, message = "关键词长度不能超过100") String keyword,
        @Size(max = 64, message = "物料编码长度不能超过64") String materialCode,
        @Size(max = 64, message = "仓库编码长度不能超过64") String warehouseCode,
        @Size(max = 32, message = "库存状态长度不能超过32") String status,
        @Min(value = 1, message = "页码必须大于0") Integer pageNum,
        @Min(value = 1, message = "每页数量必须大于0") @Max(value = 50, message = "每页数量不能超过50") Integer pageSize) {
}
