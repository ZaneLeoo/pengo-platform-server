package com.ruoyi.agent.tool.supplier;

/** 供应商工具查询条件。 */
public record SupplierToolQuery(String keyword, Boolean includeDisabled) {
    public SupplierToolQuery {
        includeDisabled = Boolean.TRUE.equals(includeDisabled);
    }
}
