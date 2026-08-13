package com.ruoyi.flow.definition.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程节点：线性审批链上的一个审批环节。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowDefinitionNode extends BaseEntity {

    /** 节点ID。 */
    private Long nodeId;

    /** 所属流程定义ID。 */
    @NotNull(message = "所属流程不能为空")
    private Long flowId;

    /** 节点名称，如"部门负责人审批"。 */
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /** 审批人配置类型：user指定登录名，role指定角色编码。 */
    private String assignType;

    /** 审批人配置值：登录名或角色编码，逗号分隔多个。 */
    @NotBlank(message = "审批人不能为空")
    private String assignValue;

    /** 审批方式：OR或签（任一同意即通过），AND会签（全部同意才通过）。 */
    private String signType;

    /** 节点顺序，从小到大依次审批。 */
    private Integer sortOrder;
}
