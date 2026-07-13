package com.ruoyi.mes.purchase.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** AI 工具使用的固定分页结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryToolPage<T> {
    private List<T> data;
    private int pageNum;
    private int pageSize;
    private long total;
    private boolean hasMore;

}
