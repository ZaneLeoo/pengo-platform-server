package com.ruoyi.agent.tool.bom;

import lombok.AllArgsConstructor;
import lombok.Data;

/** BOM 查询工具请求。 */
@Data
@AllArgsConstructor
public class BomToolQuery {
    private String keyword;
    private String bomType;
    private Boolean includeDisabled;

    public BomToolQuery() {
        this(null, null, false);
    }
}
