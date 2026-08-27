package com.ruoyi.projectmanagement.change.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 计划变更业务审计记录。 */
@Data
public class ProjectPlanChangeAudit {
    private Long auditId;
    private Long changeId;
    private String action;
    private Long operatorUserId;
    private String operator;
    private String detail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
