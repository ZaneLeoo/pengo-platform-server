package com.ruoyi.agent.tool.bom;

import lombok.Data;

/** BOM 结构查询工具请求。 */
@Data
public class BomStructureQuery {
    private Long bomId;
    private String bomCode;
    private String parentItemCode;
    private Long versionId;
    private String versionCode;
}
