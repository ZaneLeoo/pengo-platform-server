package com.ruoyi.agent.tool.materialcategory;

/** 面向 Agent 的物料分类安全输出字段。 */
public record MaterialCategoryToolItem(
        Long id,
        String code,
        String name,
        Long parentId,
        String ancestors,
        int level,
        String status) {
}
