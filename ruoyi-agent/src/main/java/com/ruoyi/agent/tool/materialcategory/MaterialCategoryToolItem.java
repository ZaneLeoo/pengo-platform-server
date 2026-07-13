package com.ruoyi.agent.tool.materialcategory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 面向 Agent 的物料分类安全输出字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCategoryToolItem {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private String ancestors;
    private int level;
    private String status;

}
