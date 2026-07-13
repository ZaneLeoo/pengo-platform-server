package com.ruoyi.agent.tool.material;

/** 面向 Agent 的物料安全输出字段。 */
public record MaterialToolItem(
    Long id,
    String code,
    String name,
    String type,
    Long categoryId,
    String categoryName,
    String spec,
    String model,
    String unit,
    String version,
    String status)
{
}
