package com.ruoyi.projectmanagement.task.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 执行任务的生命周期操作记录。 */
@Data
public class ProjectTaskOperationLog {
    private Long logId;
    private Long taskId;

    /** 操作类型：START、PAUSE、RESUME、COMPLETE。 */
    private String action;

    private String fromStatus;
    private String toStatus;

    /** 暂停原因或补充说明。 */
    private String remark;

    private Long operatorUserId;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}
