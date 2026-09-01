package com.ruoyi.projectmanagement.budget.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工作包按成本类别分配的预算明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWorkPackageBudgetLine extends BaseEntity {
    private Long workPackageBudgetLineId;
    private Long projectId;
    private Long workPackageId;
    private String workPackageCode;
    private String workPackageName;
    private String workPackageStatus;
    private Long costCategoryId;
    private String categoryCode;
    private String categoryName;
    private String categoryPath;
    private String categoryStatus;
    private BigDecimal budgetAmount;
    private String estimationBasis;
    private Integer sortOrder;

    /** 项目分类预算及同类别项目级分配汇总，均为只读展示字段。 */
    private BigDecimal projectCategoryBudget;

    private BigDecimal categoryAllocatedAmount;
    private BigDecimal categoryRemainingAmount;

    /** 该工作包该类别已发生实际成本，只读展示。 */
    private BigDecimal workPackageActualAmount;

    private BigDecimal workPackageRemainingAmount;
}
