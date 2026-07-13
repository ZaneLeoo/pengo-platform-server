package com.ruoyi.agent.tool.material;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Dify 物料查询工具请求。 */
@Data
@AllArgsConstructor
public class MaterialToolQuery {
    private String keyword;
    private Long categoryId;
    private String materialType;
    private Boolean includeDisabled;

    /** 创建默认只查询启用物料的请求。 */
    public MaterialToolQuery() {
        this(null, null, null, false);
    }
}
