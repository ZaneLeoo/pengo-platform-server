package com.ruoyi.projectmanagement.workflow.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 串行审批节点配置。 */
@Data
public class WorkflowNode {
    /** 前端画布节点键。 */
    @NotBlank private String key;

    /** 节点名称。 */
    @NotBlank private String name;

    /** 审批人类型：USER、PROJECT_ROLE。 */
    @NotBlank private String approverType;

    /** 用户ID或项目角色编码。 */
    @NotBlank private String approverValue;

    /** 画布横坐标。 */
    private Integer x;

    /** 画布纵坐标。 */
    private Integer y;
}
