package com.ruoyi.flow.engine.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.flow.engine.enums.FlowInstanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程实例：一次业务提交对应一条实例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowInstance extends BaseEntity {

    /** 实例ID。 */
    private Long instanceId;

    /** 流程定义ID。 */
    @NotNull(message = "流程不能为空")
    private Long flowId;

    /** 流程编码（冗余）。 */
    private String flowKey;

    /** 流程名称（冗余）。 */
    private String flowName;

    /** 业务类型编码。 */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** 业务记录ID。 */
    @NotNull(message = "业务ID不能为空")
    private Long bizId;

    /** 业务编码（展示用）。 */
    private String bizCode;

    /** 业务名称（展示用）。 */
    private String bizName;

    /** 当前节点ID，实例完成后为空。 */
    private Long currentNodeId;

    /** 当前节点名称。 */
    private String currentNodeName;

    /** 实例状态（FlowInstanceStatus）。 */
    private String status;

    /** 发起人登录名。 */
    private String submitBy;

    /** 发起人姓名。 */
    private String submitName;

    /** 发起时间。 */
    private java.util.Date submitTime;

    /** 完成时间（通过/驳回/撤销）。 */
    private java.util.Date finishTime;
}
