package com.ruoyi.projectmanagement.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目初步计划（立项阶段）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPreliminaryPlan extends BaseEntity {

    private Long planId;

    /** 所属项目ID，由接口路径或旧记录提供。 */
    private Long projectId;

    @NotBlank
    private String phaseName;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotBlank
    private String milestoneName;

    private String phaseGoal;

    private Integer sortOrder;

    /** 已转换的正式阶段ID。 */
    private Long convertedPhaseId;
}
