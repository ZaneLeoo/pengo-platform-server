package com.ruoyi.projectmanagement.budget.domain;

import java.math.BigDecimal;
import lombok.Data;

/** 实际成本按类别或工作包的聚合结果。 */
@Data
public class ProjectActualCostAggregate {
    private Long costCategoryId;
    private Long workPackageId;
    private BigDecimal actualAmount;
}
