package com.ruoyi.projectmanagement.deliverable.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工作包的正式应交付项；交付物提交后进入审批闭环并参与工作包验收。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDeliverable extends BaseEntity {

    /** 交付物ID。 */
    private Long deliverableId;

    /** 所属项目ID。 */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    /** 项目名称（冗余展示字段）。 */
    private String projectName;

    /** 项目状态（列表展示与操作控制字段）。 */
    private String projectStatus;

    /** 所属工作包ID。 */
    @NotNull(message = "所属工作包不能为空")
    private Long workPackageId;

    /** 列表筛选用：汇总WBS或项目节点下的工作包ID集合，不落库。 */
    private List<Long> workPackageIds;

    /** 工作包名称（冗余展示字段）。 */
    private String workPackageName;

    /** 工作包负责人姓名（冗余展示字段）。 */
    private String workPackageOwnerName;

    /** 工作包负责人档案编码（冗余展示字段）。 */
    private String workPackageOwnerCode;

    /** 工作包负责人登录账号（前端展示提交入口时的权限判断字段）。 */
    private String workPackageOwnerUserName;

    /** 工作包负责人ID（我的待交付筛选字段）。 */
    private Long workPackageOwnerId;

    /** 工作包负责人系统用户ID（我的待交付筛选字段）。 */
    private Long workPackageOwnerUserId;

    /** 交付物名称。 */
    @NotBlank(message = "交付物名称不能为空")
    private String deliverableName;

    /** 交付物类型：DOCUMENT、DRAWING、BOM、PROCESS、REPORT、FORM、EXTERNAL_LINK、OTHER。 */
    @NotBlank(message = "交付物类型不能为空")
    private String deliverableType;

    /** 来源交付物类型配置。 */
    private Long deliverableTypeId;

    /** 类型名称快照。 */
    private String deliverableTypeName;

    /** 提交方式快照：FILE、LINK、BUSINESS_OBJECT。 */
    private String submissionMode;

    /** 允许文件扩展名快照，逗号分隔；LINK类型为空。 */
    private String allowedExtensions;

    /** 是否必交：0选交，1必交。 */
    private String requiredFlag;

    /** 是否需审批：0不需审批，1需审批。 */
    private String approvalRequired;

    /** 审核人登录编码（冗余展示字段）。 */
    private String reviewer;

    /** 交付物状态：PENDING、DELIVERED、PENDING_APPROVAL、APPROVED、RETURNED（DeliverableStatus）。 */
    private String status;

    /** 说明。 */
    private String description;

    /** 计划交付日期。 */
    private LocalDate plannedDate;

    /** 验收标准。 */
    private String acceptanceCriteria;

    /** 最近提交人（冗余展示字段）。 */
    private String submitBy;

    /** 审核人姓名（冗余展示字段）。 */
    private String reviewerName;

    /** 最近提交的文件地址。 */
    private String latestFileUrl;

    /** 最近提交的外部链接。 */
    private String latestExternalUrl;

    /** 最近交付的业务对象类型；BOM 交付固定为 BOM_VERSION。 */
    private String businessType;

    /** 最近交付的业务对象ID；BOM 交付关联 bom_version.id。 */
    private String businessId;

    /** 最近交付的业务对象编码。 */
    private String businessCode;

    /** 最近交付的业务对象名称。 */
    private String businessName;

    /** 最近交付的业务对象版本。 */
    private String businessVersion;

    /** 全局交付物列表的数据范围过滤用户，不对前端返回。 */
    @JsonIgnore private Long viewerUserId;
}
