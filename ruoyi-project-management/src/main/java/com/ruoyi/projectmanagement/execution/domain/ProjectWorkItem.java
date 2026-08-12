package com.ruoyi.projectmanagement.execution.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目执行项，统一承载任务、交付物和问题。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWorkItem extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long itemId;
    @NotNull(message = "所属项目不能为空")
    private Long projectId;
    private String projectName;
    private Long parentId;
    /** 关联WBS任务ID，供交付物和问题追溯来源任务。 */
    private Long taskId;
    /** 关联WBS任务名称，仅用于展示。 */
    private String taskName;
    /** 是否要求交付物：0 否，1 是，仅供WBS任务使用。 */
    private String deliverableRequired;
    /** TASK、DELIVERABLE、ISSUE。 */
    @NotBlank(message = "执行项类型不能为空")
    private String itemType;
    @NotBlank(message = "编码不能为空")
    private String itemCode;
    @NotBlank(message = "名称不能为空")
    private String itemName;
    private Long ownerId;
    private String ownerName;
    /** 任务/交付物/问题各自的生命周期状态。 */
    @NotBlank(message = "状态不能为空")
    private String status;
    /** LOW、MEDIUM、HIGH、CRITICAL，主要供问题使用。 */
    private String priority;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private Integer progress;
    private String fileUrl;
    private String description;
    private Integer sortOrder;
}
