package com.ruoyi.projectmanagement.budget.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 项目预算执行汇总。 */
@Data
public class ProjectBudgetExecutionSummary {
    private Long projectId;
    private String budgetRequired;
    private BigDecimal totalBudget;
    private BigDecimal totalAllocated;
    private BigDecimal totalActualCost;
    private BigDecimal totalRemaining;
    private BigDecimal executionRate;
    private List<ProjectBudgetExecutionRow> rows;
}
