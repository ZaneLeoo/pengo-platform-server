package com.ruoyi.agent.tool.bom;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 指定 BOM 版本的结构化输出。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomStructureToolResult {
    private Long bomId;
    private String bomCode;
    private String parentMaterialCode;
    private String parentMaterialName;
    private String parentMaterialSpec;
    private String parentMaterialUnit;
    private String bomType;
    private String bomStatus;
    private BomVersionToolItem version;
    private List<BomComponentToolItem> components;
}
