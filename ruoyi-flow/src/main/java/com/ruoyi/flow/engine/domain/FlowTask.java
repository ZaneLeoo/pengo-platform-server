package com.ruoyi.flow.engine.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批待办任务：一个节点按审批人拆分为多条任务。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTask extends BaseEntity {

    /** 任务ID。 */
    private Long taskId;

    /** 所属流程实例ID。 */
    @NotNull(message = "流程实例不能为空")
    private Long instanceId;

    /** 节点ID。 */
    private Long nodeId;

    /** 节点名称（冗余）。 */
    private String nodeName;

    /** 审批人登录名。 */
    @NotBlank(message = "审批人不能为空")
    private String assignee;

    /** 审批人姓名。 */
    private String assigneeName;

    /** 状态：PENDING待审批，APPROVED已同意，REJECTED已驳回，SKIPPED已跳过。 */
    private String status;

    /** 审批意见。 */
    private String comment;

    /** 处理时间。 */
    private java.util.Date approveTime;
}
