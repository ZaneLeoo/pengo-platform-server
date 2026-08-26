package com.ruoyi.projectmanagement.task.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工作包内的任务节点；汇总任务由下级自动汇总，执行任务承载具体工作。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectTask extends BaseEntity {

    /** 任务ID。 */
    private Long taskId;

    /** 所属项目ID。 */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    /** 所属项目名称（冗余展示字段）。 */
    private String projectName;

    /** 所属工作包ID。 */
    @NotNull(message = "所属工作包不能为空")
    private Long workPackageId;

    /** 工作包名称（冗余展示字段）。 */
    private String workPackageName;

    /** 所属WBS名称（工作包的上级范围）。 */
    private String wbsName;

    /** 上级任务ID，0表示工作包下的顶层任务。 */
    private Long parentTaskId;

    /** 任务层级编码，如 1-T1、1.1、1.1.2。 */
    private String taskCode;

    /** 任务类型：SUMMARY汇总任务、EXECUTION执行任务（TaskType）。 */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    /** 任务名称。 */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /** 任务说明。 */
    private String description;

    /** 执行人ID（仅执行任务）。 */
    private Long assigneeId;

    /** 执行人系统用户ID。 */
    private Long assigneeUserId;

    /** 执行人姓名（冗余展示字段）。 */
    private String assigneeName;

    /** 执行人档案编码。 */
    private String assigneeCode;

    /** 执行人登录账号。 */
    private String assigneeUserName;

    /** 计划开始日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    /** 计划结束日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    /** 实际开始日期，执行开始动作时记录。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    /** 实际完成日期，执行完成动作时记录。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    /** 预计工时。 */
    private BigDecimal estimatedHours;

    /** 实际工时。 */
    private BigDecimal actualHours;

    /** 优先级。 */
    private String priority;

    /** 任务状态：NOT_STARTED、ACTIVE、PAUSED、COMPLETED（WorkItemStatus）。 */
    private String status;

    /** 进度百分比 0-100，汇总任务为下级均值。 */
    private Integer progress;

    /** 暂停原因，暂停动作时必填。 */
    private String pauseReason;

    /** 同级排序号。 */
    private Integer sortOrder;

    /** 下级任务数（冗余展示字段）。 */
    private Integer childCount;
}
