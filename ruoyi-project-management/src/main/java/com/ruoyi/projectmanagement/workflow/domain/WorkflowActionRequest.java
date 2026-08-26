package com.ruoyi.projectmanagement.workflow.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 审批任务处理请求。 */
@Data
public class WorkflowActionRequest {
    /** APPROVE 或 REJECT。 */
    @NotBlank(message = "审批动作不能为空")
    private String action;

    /** 审批意见，驳回时必填。 */
    private String opinion;
}
