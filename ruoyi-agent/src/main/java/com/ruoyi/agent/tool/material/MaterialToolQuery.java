package com.ruoyi.agent.tool.material;

/** Dify 物料查询工具请求。 */
public record MaterialToolQuery(String keyword, Long categoryId, String materialType, Boolean includeDisabled) {
    /** 创建默认只查询启用物料的请求。 */
    public MaterialToolQuery() {
        this(null, null, null, false);
    }
}
