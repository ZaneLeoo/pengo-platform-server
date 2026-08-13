package com.ruoyi.projectmanagement.issue.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 可关联工作包或任务的项目问题。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectIssue extends BaseEntity {
    private Long issueId;
    @NotNull(message = "所属项目不能为空") private Long projectId;
    private Long workPackageId;
    private String workPackageName;
    private Long taskId;
    private String taskName;
    private String issueCode;
    @NotBlank(message = "问题名称不能为空") private String issueName;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String severity;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate dueDate;
    private String resolution;
}
