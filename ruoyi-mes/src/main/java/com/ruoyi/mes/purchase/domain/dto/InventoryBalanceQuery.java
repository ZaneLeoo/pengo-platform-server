package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** AI 查询库存余额的筛选条件。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBalanceQuery {
    private @Size(max = 100, message = "关键词长度不能超过100") String keyword;
    private @Size(max = 64, message = "物料编码长度不能超过64") String materialCode;
    private @Size(max = 64, message = "仓库编码长度不能超过64") String warehouseCode;
    private @Size(max = 32, message = "库存状态长度不能超过32") String status;
    private @Size(max = 16, message = "效期状态长度不能超过16") String expiryStatus;
    private @Min(value = 0, message = "临期天数不能小于0") @Max(value = 3650, message = "临期天数不能超过3650") Integer expiresWithinDays;
    private @Min(value = 1, message = "页码必须大于0") Integer pageNum;
    private @Min(value = 1, message = "每页数量必须大于0") @Max(value = 50, message = "每页数量不能超过50") Integer pageSize;

}
