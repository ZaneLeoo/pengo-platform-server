package com.ruoyi.projectmanagement.notification.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 从审批、任务、交付物和问题实时聚合的个人待办。 */
@Data
public class ProjectTodoItem {
    private String itemKey;
    private String itemType;
    private String title;
    private String description;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private String targetPath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
