package com.ruoyi.projectmanagement.budget.domain;

import java.math.BigDecimal;
import lombok.Data;

/** 项目预算执行的单类别汇总行。 */
@Data
public class ProjectBudgetExecutionRow {
    private Long costCategoryId;
    private String categoryCode;
    private String categoryName;
    private String categoryPath;
    private String categoryStatus;

    /** 项目分类预算。 */
    private BigDecimal projectBudget;

    /** 已分配到工作包的合计。 */
    private BigDecimal allocatedAmount;

    /** 项目分类预算减去工作包分配。 */
    private BigDecimal remainingAllocation;

    /** 该类别已发生实际成本合计。 */
    private BigDecimal actualCostAmount;

    /** 项目分类预算减去实际成本。 */
    private BigDecimal remainingBudget;

    private BigDecimal executionRate;
}
