package com.ruoyi.projectmanagement.change.domain;

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
    private LocalDateTime createTime;
}
