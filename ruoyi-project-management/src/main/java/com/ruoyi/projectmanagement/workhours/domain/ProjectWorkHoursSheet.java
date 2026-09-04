package com.ruoyi.projectmanagement.workhours.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 一人一自然周的工时单。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWorkHoursSheet extends BaseEntity {
    private Long sheetId;
    private Long userId;
    private String userName;
    private String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;

    private Long projectId;
    private String sheetType;
    private String status;
    private String lateReportReason;
    private Long workflowInstanceId;
    private java.time.LocalDateTime submitTime;
    private java.time.LocalDateTime archiveTime;
    private List<ProjectWorkHoursEntry> entries = new ArrayList<>();
}
