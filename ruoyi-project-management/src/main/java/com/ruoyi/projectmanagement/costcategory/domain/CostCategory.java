package com.ruoyi.projectmanagement.costcategory.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目成本类别。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostCategory extends BaseEntity {

    private Long costCategoryId;
    private Long parentId;
    private String ancestors;

    @NotBlank(message = "成本类别编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "成本类别编码只能包含字母、数字和下划线")
    @Size(max = 50, message = "成本类别编码长度不能超过50个字符")
    private String categoryCode;

    @NotBlank(message = "成本类别名称不能为空")
    @Size(max = 100, message = "成本类别名称长度不能超过100个字符")
    private String categoryName;

    private Integer levelNo;

    /** 0 不允许，1 允许。 */
    private String allowManualEntry;

    @Size(max = 64, message = "财务科目编码长度不能超过64个字符")
    private String financeAccountCode;

    @Size(max = 128, message = "财务科目名称长度不能超过128个字符")
    private String financeAccountName;

    /** 1 系统预置，0 自定义。 */
    private String systemFlag;

    /** 0 启用，1 停用。 */
    private String status;

    private Integer sortOrder;

    @Size(max = 500, message = "成本类别说明长度不能超过500个字符")
    private String description;

    private Boolean leaf;
    private String effectiveStatus;
    private String fullPath;
    private Long usageCount;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canAddChild;
    private String readonlyReason;
    private List<CostCategory> children = new ArrayList<>();
}
