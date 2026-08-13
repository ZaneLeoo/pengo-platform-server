package com.ruoyi.projectmanagement.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目团队成员实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectMember extends BaseEntity {

    private Long memberId;

    @NotNull
    private Long projectId;

    @NotNull
    private Long personId;

    private String personCode;

    private String personName;

    private String deptName;

    @NotNull
    private Long roleId;

    private String roleCode;

    private String roleName;

    /** 专业角色，如项目经理、机械设计、电气设计等。 */
    private String specialtyRole;

    /** 职责说明。 */
    private String responsibility;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exitDate;

    /** ACTIVE 在组 / EXITED 已退出。 */
    private String status;

    /** 任务统计：总任务数。 */
    private Integer taskCount;

    /** 任务统计：已完成任务数。 */
    private Integer completedTaskCount;

    /** 任务统计：逾期任务数。 */
    private Integer overdueTaskCount;
}
