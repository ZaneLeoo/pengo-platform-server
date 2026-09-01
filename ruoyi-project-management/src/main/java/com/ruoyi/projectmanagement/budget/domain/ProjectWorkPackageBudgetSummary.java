package com.ruoyi.projectmanagement.budget.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 项目或单个工作包的预算分配汇总。 */
@Data
public class ProjectWorkPackageBudgetSummary {
    private Long projectId;
    private Long workPackageId;
    private BigDecimal allocatedAmount;
    private Integer categoryCount;
    private List<ProjectWorkPackageBudgetLine> lines;
}
