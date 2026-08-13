package com.ruoyi.projectmanagement.deliverable.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作包的正式应交付项。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDeliverable extends BaseEntity {

    private Long deliverableId;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private String projectName;

    @NotNull(message = "所属工作包不能为空")
    private Long workPackageId;

    private String workPackageName;

    @NotBlank(message = "交付物名称不能为空")
    private String deliverableName;

    @NotBlank(message = "交付物类型不能为空")
    private String deliverableType;

    /** 0选交，1必交。 */
    private String requiredFlag;

    /** 0不需审批，1需审批。 */
    private String approvalRequired;

    /** 固定 admin，预留后续扩展。 */
    private String reviewer = "admin";

    /** PENDING、DELIVERED、PENDING_APPROVAL、APPROVED、RETURNED。 */
    private String status;

    private String description;

    private java.time.LocalDate plannedDate;
    private String acceptanceCriteria;
    private String businessType;
    private String businessId;

    private String submitBy;

    private String reviewerName;

    private String latestFileUrl;

    private String latestExternalUrl;
}
