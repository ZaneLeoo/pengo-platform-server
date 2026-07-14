package com.ruoyi.agent.tool.bom;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BOM 子件工具输出。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomComponentToolItem {
    private Long id;
    private Integer lineNumber;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String specification;
    private String unit;
    private String attribute;
    private BigDecimal quantity;
    private BigDecimal fixedLossQuantity;
    private BigDecimal variableLossRate;
    private String supplyType;
    private Boolean virtualComponent;
    private Boolean mrpExpand;
    private Boolean hasChildBom;
    private Long childBomVersionId;
}
