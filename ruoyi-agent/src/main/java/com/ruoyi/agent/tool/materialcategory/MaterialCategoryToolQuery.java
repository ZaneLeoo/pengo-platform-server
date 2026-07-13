package com.ruoyi.agent.tool.materialcategory;

/** Dify 物料分类查询工具请求。 */
public record MaterialCategoryToolQuery(String keyword, Long parentId, Boolean includeDisabled)
{
    /** 创建默认只查询启用分类的请求。 */
    public MaterialCategoryToolQuery()
    {
        this(null, null, false);
    }
}
