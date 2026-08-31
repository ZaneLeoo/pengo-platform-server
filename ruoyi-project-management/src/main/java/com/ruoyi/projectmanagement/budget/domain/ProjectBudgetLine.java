package com.ruoyi.projectmanagement.budget.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/** 项目成本类别预算明细。 */
@Data
public class ProjectBudgetLine {
    private Long budgetLineId;
    private Long projectId;

    @NotNull(message = "成本类别不能为空")
    private Long costCategoryId;

    private String categoryCode;
    private String categoryName;
    private String categoryPath;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    @Digits(integer = 14, fraction = 2, message = "预算金额最多14位整数和2位小数")
    private BigDecimal budgetAmount;

    @NotBlank(message = "测算依据不能为空")
    @Size(max = 1000, message = "测算依据不能超过1000个字符")
    private String estimationBasis;

    private Integer sortOrder;
    private String categoryStatus;
    private String createBy;
    private String updateBy;
}
