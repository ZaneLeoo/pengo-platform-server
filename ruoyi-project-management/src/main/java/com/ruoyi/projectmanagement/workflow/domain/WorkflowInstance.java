package com.ruoyi.projectmanagement.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审批实例及统一任务详情。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowInstance extends BaseEntity {
    private Long instanceId;
    private String businessType;
    private Long businessId;
    private Long projectId;
    private Long definitionVersionId;
    private String title;
    private Long initiatorUserId;
    private String initiatorName;
    private String status;
    private String currentNodeKey;
    private String businessSnapshotJson;
    private LocalDateTime finishTime;
    private List<WorkflowTask> tasks;
}
