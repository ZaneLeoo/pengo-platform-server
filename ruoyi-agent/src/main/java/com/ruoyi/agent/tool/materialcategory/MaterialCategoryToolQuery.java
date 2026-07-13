package com.ruoyi.agent.tool.materialcategory;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Dify 物料分类查询工具请求。 */
@Data
@AllArgsConstructor
public class MaterialCategoryToolQuery {
    private String keyword;
    private Long parentId;
    private Boolean includeDisabled;

    /** 创建默认只查询启用分类的请求。 */
    public MaterialCategoryToolQuery() {
        this(null, null, false);
    }
}
