package com.ruoyi.projectmanagement.budget.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 项目预算详情和汇总。 */
@Data
public class ProjectBudgetSummary {
    private Long projectId;
    private String budgetRequired;
    private BigDecimal budgetAmount;
    private String budgetDescription;
    private BigDecimal allocatedAmount;
    private BigDecimal differenceAmount;
    private BigDecimal initialApprovedAmount;
    private BigDecimal cumulativeChangeAmount;
    private Integer categoryCount;
    private List<ProjectBudgetLine> lines;

    /** 项目实际成本合计。 */
    private BigDecimal actualCostAmount;

    /** 预算总额减去实际成本。 */
    private BigDecimal remainingBudgetAmount;

    /** 实际成本占预算总额的百分比。 */
    private BigDecimal executionRate;
}
