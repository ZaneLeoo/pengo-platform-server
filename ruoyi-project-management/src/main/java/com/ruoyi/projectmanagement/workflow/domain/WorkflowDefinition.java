package com.ruoyi.projectmanagement.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审批流定义及当前编辑版本。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowDefinition extends BaseEntity {
    /** 定义ID。 */
    private Long definitionId;
    /** 定义名称。 */
    @NotBlank
    private String definitionName;
    /** 业务类型：PROJECT_INITIATION、DELIVERABLE_APPROVAL。 */
    @NotBlank
    private String businessType;
    /** 当前发布版本ID。 */
    private Long activeVersionId;
    /** ENABLED、DISABLED。 */
    private String status;
    /** 当前展示版本ID。 */
    private Long versionId;
    /** 版本号。 */
    private Integer versionNo;
    /** DRAFT、PUBLISHED。 */
    private String versionStatus;
    /** 数据库存储的节点JSON，仅供服务层转换。 */
    private String graphJson;
    /** 严格按数组顺序执行的审批节点。 */
    private List<WorkflowNode> nodes;
}
