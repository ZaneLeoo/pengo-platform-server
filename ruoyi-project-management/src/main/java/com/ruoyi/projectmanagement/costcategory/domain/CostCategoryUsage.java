package com.ruoyi.projectmanagement.costcategory.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 成本类别引用及操作能力。 */
@Data
@AllArgsConstructor
public class CostCategoryUsage {
    private Long usageCount;
    private Boolean canDelete;
    private Boolean canMove;
    private Boolean canAddChild;
    private String readonlyReason;
}
