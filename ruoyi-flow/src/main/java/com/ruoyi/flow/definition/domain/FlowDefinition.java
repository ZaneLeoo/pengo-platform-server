package com.ruoyi.flow.definition.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程定义。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowDefinition extends BaseEntity {

    /** 流程定义ID。 */
    private Long flowId;

    /** 流程编码，全局唯一。 */
    @NotBlank(message = "流程编码不能为空")
    private String flowKey;

    /** 流程名称。 */
    @NotBlank(message = "流程名称不能为空")
    private String flowName;

    /** 状态：0草稿，1启用（启用后允许发起实例，修改前需停用）。 */
    private String status;

    /** 备注。 */
    private String remark;
}
