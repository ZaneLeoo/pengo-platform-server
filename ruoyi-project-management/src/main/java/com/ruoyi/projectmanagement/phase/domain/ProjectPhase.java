package com.ruoyi.projectmanagement.phase.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目阶段实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPhase extends BaseEntity {

    private Long phaseId;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private String projectName;

    @NotBlank(message = "阶段名称不能为空")
    private String phaseName;

    /** 阶段编码，由初步计划转换时生成。 */
    private String phaseCode;

    /** 阶段负责人档案ID。 */
    private Long ownerId;

    private String ownerName;

    /** 阶段负责人登录账号，仅用于权限判断。 */
    private String ownerCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    /** NOT_STARTED、ACTIVE、PAUSED、COMPLETED。 */
    private String status;

    private Integer sortOrder;

    /** 任务统计：任务总数。 */
    private Integer taskCount;

    /** 任务统计：已完成任务数。 */
    private Integer completedTaskCount;
}
