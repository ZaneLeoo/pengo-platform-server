package com.ruoyi.projectmanagement.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目主档。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /** 项目ID。 */
    private Long projectId;
    /** 项目编码，项目管理范围内唯一。 */
    @Excel(name = "项目编码")
    private String projectCode;
    /** 项目名称。 */
    @Excel(name = "项目名称")
    private String projectName;
    /** 项目分类ID。 */
    private Long categoryId;
    /** 项目分类名称，仅用于展示。 */
    @Excel(name = "项目分类")
    private String categoryName;
    /** 项目负责人档案ID。 */
    private Long managerId;
    /** 项目负责人姓名，仅用于展示。 */
    @Excel(name = "项目负责人")
    private String managerName;
    /** 负责人所属部门，仅用于展示。 */
    private String managerDeptName;
    /** 计划开始日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始日期")
    private LocalDate startDate;
    /** 计划结束日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束日期")
    private LocalDate endDate;
    /** 状态：DRAFT 草稿，PLANNED 未开始，ACTIVE 执行中，PAUSED 已暂停，COMPLETED 已完成。 */
    @Excel(name = "状态")
    private String status;
    /** 项目总体进度，0-100。 */
    @Excel(name = "进度")
    private Integer progress;
    /** 项目目标。 */
    private String projectGoal;

    @NotBlank(message = "项目编码不能为空")
    @Size(max = 32, message = "项目编码长度不能超过32个字符")
    public String getProjectCode() {
        return projectCode;
    }
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    public String getProjectName() {
        return projectName;
    }
    @NotNull(message = "项目分类不能为空")
    public Long getCategoryId() {
        return categoryId;
    }
    @NotNull(message = "项目负责人不能为空")
    public Long getManagerId() {
        return managerId;
    }
    @NotNull(message = "计划开始日期不能为空")
    public LocalDate getStartDate() {
        return startDate;
    }
    @NotNull(message = "计划结束日期不能为空")
    public LocalDate getEndDate() {
        return endDate;
    }
    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    public Integer getProgress() {
        return progress;
    }
    @NotBlank(message = "项目目标不能为空")
    @Size(max = 1000, message = "项目目标长度不能超过1000个字符")
    public String getProjectGoal() {
        return projectGoal;
    }
}
