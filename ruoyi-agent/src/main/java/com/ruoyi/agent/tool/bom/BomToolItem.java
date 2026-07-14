package com.ruoyi.agent.tool.bom;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BOM 及其版本摘要工具输出。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomToolItem {
    private Long id;
    private String code;
    private Long parentMaterialId;
    private String parentMaterialCode;
    private String parentMaterialName;
    private String parentMaterialSpec;
    private String parentMaterialUnit;
    private String type;
    private String status;
    private List<BomVersionToolItem> versions;
}
