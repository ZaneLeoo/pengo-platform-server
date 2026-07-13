package com.ruoyi.agent.tool.material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 面向 Agent 的物料安全输出字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialToolItem {
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long categoryId;
    private String categoryName;
    private String spec;
    private String model;
    private String unit;
    private String version;
    private String status;

}
