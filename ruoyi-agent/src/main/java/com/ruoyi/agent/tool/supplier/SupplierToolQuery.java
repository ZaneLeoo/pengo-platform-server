package com.ruoyi.agent.tool.supplier;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 供应商工具查询条件。 */
@Data
@NoArgsConstructor
public class SupplierToolQuery {
    private String keyword;
    private Boolean includeDisabled;

    public SupplierToolQuery(String keyword, Boolean includeDisabled) {
        this.keyword = keyword;
        this.includeDisabled = Boolean.TRUE.equals(includeDisabled);
    }
}
