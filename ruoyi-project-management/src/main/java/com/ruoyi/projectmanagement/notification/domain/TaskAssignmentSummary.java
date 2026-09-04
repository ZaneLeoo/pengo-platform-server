package com.ruoyi.projectmanagement.notification.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

/** 项目启动时按任务执行人汇总的任务分配信息。 */
@Data
public class TaskAssignmentSummary {
    private Long userId;
    private Integer taskCount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastEndDate;
}
