package com.ruoyi.projectmanagement.workflow.domain;

import java.time.LocalDateTime;
import lombok.Data;

/** 用户审批任务。 */
@Data
public class WorkflowTask {
    private Long taskId;
    private Long instanceId;
    private String nodeKey;
    private String nodeName;
    private Integer nodeOrder;
    private String status;
    private Long actedByUserId;
    private String actedByName;
    private String opinion;
    private LocalDateTime actedTime;
    private LocalDateTime createTime;
    /** 发起实例时固定下来的候选审批人姓名，使用顿号分隔。 */
    private String candidateNames;
    /** 审批来源：USER、PROJECT_ROLE。 */
    private String approverType;
    /** 审批来源的可读名称。 */
    private String approverLabel;
    private WorkflowInstance instance;
    /** 待办列表中的流程标题。 */
    private String title;
    /** 待办列表中的业务类型。 */
    private String businessType;
    /** 所属项目ID。 */
    private Long projectId;
    /** 发起人ID。 */
    private Long initiatorUserId;
    /** 发起人姓名。 */
    private String initiatorName;
    /** 实例状态。 */
    private String instanceStatus;
}
