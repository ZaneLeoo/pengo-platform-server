package com.ruoyi.projectmanagement.change.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目计划变更单。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPlanChange extends BaseEntity {
    private Long changeId;
    private String changeCode;
    private Long projectId;
    private String projectName;
    private Long baseBaselineId;
    private Integer baseVersionNo;
    private String title;
    private String changeReason;
    private String impactDescription;
    private String status;
    private Long workflowInstanceId;
    private Long applicantUserId;
    private String applicantName;
    private List<ProjectPlanChangeItem> items;
    private List<ProjectPlanChangeAttachment> attachments;
    private ProjectPlanChangeCapability capability;
    private List<ProjectPlanChangeAudit> audits;

    /** 从业务审计推导的提交时间，列表展示用，不单独落库。 */
    private LocalDateTime submitTime;

    /** 从业务审计推导的应用时间，列表展示用，不单独落库。 */
    private LocalDateTime applyTime;
}
