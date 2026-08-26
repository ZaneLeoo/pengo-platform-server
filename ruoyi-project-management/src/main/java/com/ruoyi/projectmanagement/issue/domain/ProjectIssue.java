package com.ruoyi.projectmanagement.issue.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目执行过程中的问题跟踪记录，可关联工作包或任务。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectIssue extends BaseEntity {

    /** 问题ID。 */
    private Long issueId;

    /** 所属项目ID。 */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    /** 关联工作包ID，可空。 */
    private Long workPackageId;

    /** 工作包名称（冗余展示字段）。 */
    private String workPackageName;

    /** 关联任务ID，可空；关联任务时工作包自动取任务所属工作包。 */
    private Long taskId;

    /** 任务名称（冗余展示字段）。 */
    private String taskName;

    /** 问题编码，项目内按序生成（如 ISS-001）。 */
    private String issueCode;

    /** 问题名称。 */
    @NotBlank(message = "问题名称不能为空")
    private String issueName;

    /** 问题说明。 */
    private String description;

    /** 提出人用户ID，由当前登录用户写入。 */
    private Long reporterUserId;

    /** 提出人姓名（展示字段）。 */
    private String reporterName;

    /** 负责人ID，必须是当前项目在组成员。 */
    private Long ownerId;

    /** 负责人姓名（冗余展示字段）。 */
    private String ownerName;

    /** 严重程度：HIGH、MEDIUM、LOW（IssueSeverity）。 */
    private String severity;

    /** 状态：OPEN、PROCESSING、RESOLVED、CLOSED（IssueStatus）。 */
    private String status;

    /** 截止日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    /** 解决方案。 */
    private String resolution;

    /** 是否已逾期（展示字段）。 */
    private Boolean overdue;

    /** 当前登录人是否可编辑基本信息。 */
    private Boolean canEdit;

    /** 当前登录人是否可删除。 */
    private Boolean canDelete;

    /** 当前登录人是否可补充动态。 */
    private Boolean canAddActivity;

    /** 当前登录人允许执行的目标状态。 */
    private List<String> allowedTransitions;

    /** 数据范围查询使用，不对持久化开放。 */
    private Long viewerUserId;

    /** 新建问题时随首条动态保存的附件名称，不持久化到问题表。 */
    private String initialAttachmentName;

    /** 新建问题时随首条动态保存的附件地址，不持久化到问题表。 */
    private String initialAttachmentUrl;
}
