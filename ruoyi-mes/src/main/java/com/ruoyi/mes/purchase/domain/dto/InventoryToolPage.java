package com.ruoyi.mes.purchase.domain.dto;

import java.util.List;

/** AI 工具使用的固定分页结果。 */
public record InventoryToolPage<T>(List<T> data, int pageNum, int pageSize, long total, boolean hasMore) {
}
