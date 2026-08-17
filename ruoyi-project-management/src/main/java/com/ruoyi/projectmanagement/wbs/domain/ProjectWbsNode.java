package com.ruoyi.projectmanagement.wbs.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目WBS范围节点；SUMMARY为汇总WBS，WORK_PACKAGE为工作包（最低管理单元）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWbsNode extends BaseEntity {

    /** WBS节点ID。 */
    private Long wbsId;

    /** 所属项目ID。 */
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    /** 上级WBS节点ID，0表示顶层节点。 */
    private Long parentId;

    /** WBS层级编码，如 1、1.1、1.1.2。 */
    private String wbsCode;

    /** 节点类型：SUMMARY汇总WBS、WORK_PACKAGE工作包（WbsNodeType）。 */
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    /** WBS名称。 */
    @NotBlank(message = "WBS名称不能为空")
    private String wbsName;

    /** 范围说明。 */
    private String scopeDescription;

    /** 负责人ID（仅工作包）。 */
    private Long ownerId;

    /** 负责人姓名（冗余展示字段）。 */
    private String ownerName;

    /** 负责人档案编码。 */
    private String ownerCode;

    /** 负责人登录账号，用于前端操作权限判断。 */
    private String ownerUserName;

    /** 计划开始日期（工作包必填）。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    /** 计划结束日期（工作包必填）。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    /** 立项批准的目标开始日期窗口。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetStartDate;

    /** 立项批准的目标结束日期窗口。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetEndDate;

    /** 立项批准的关键里程碑。 */
    private String targetMilestone;

    /** 验收标准（工作包必填）。 */
    private String acceptanceCriteria;

    /** 完成定义（工作包必填）。 */
    private String definitionOfDone;

    /** 是否需要正式交付物：0不需要，1需要（仅工作包）。 */
    private String deliverableRequired;

    /** 优先级。 */
    private String priority;

    /** 预计工时。 */
    private BigDecimal estimatedHours;

    /** 预算金额。 */
    private BigDecimal budgetAmount;

    /** 节点状态：NOT_STARTED、ACTIVE、WAITING_DELIVERY、COMPLETED（WbsStatus）。 */
    private String status;

    /** 进度百分比 0-100，汇总节点为下级均值。 */
    private Integer progress;

    /** 同级排序号。 */
    private Integer sortOrder;

    /** 下级WBS节点数（冗余展示字段）。 */
    private Integer childCount;

    /** 任务数（冗余展示字段）。 */
    private Integer taskCount;

    /** 交付物数（冗余展示字段）。 */
    private Integer deliverableCount;
}
