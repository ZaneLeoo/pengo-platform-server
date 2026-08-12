package com.ruoyi.projectmanagement.phase.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPhase extends BaseEntity {
    private Long phaseId;
    @NotNull(message = "所属项目不能为空") private Long projectId;
    private String projectName;
    @NotBlank(message = "阶段名称不能为空") private String phaseName;
    private String phaseCode;
    private Long ownerId;
    private String ownerName;
    private String ownerCode;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate endDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate actualStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate actualEndDate;
    private String status;
    private Integer sortOrder;
    private Integer taskCount;
    private Integer completedTaskCount;
}
