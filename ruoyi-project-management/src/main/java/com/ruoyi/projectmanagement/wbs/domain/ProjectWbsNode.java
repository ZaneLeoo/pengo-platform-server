package com.ruoyi.projectmanagement.wbs.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目WBS范围节点；WORK_PACKAGE表示工作包。 */
@Data @EqualsAndHashCode(callSuper = true)
public class ProjectWbsNode extends BaseEntity {
    private Long wbsId;
    @NotNull(message="所属项目不能为空") private Long projectId;
    private Long parentId;
    private String wbsCode;
    @NotBlank(message="节点类型不能为空") private String nodeType;
    @NotBlank(message="WBS名称不能为空") private String wbsName;
    private String scopeDescription;
    private Long ownerId;
    private String ownerName;
    private String ownerCode;
    @JsonFormat(pattern="yyyy-MM-dd") private LocalDate planStartDate;
    @JsonFormat(pattern="yyyy-MM-dd") private LocalDate planEndDate;
    @JsonFormat(pattern="yyyy-MM-dd") private LocalDate targetStartDate;
    @JsonFormat(pattern="yyyy-MM-dd") private LocalDate targetEndDate;
    private String targetMilestone;
    private String acceptanceCriteria;
    private String definitionOfDone;
    private String priority;
    private BigDecimal estimatedHours;
    private BigDecimal budgetAmount;
    private String status;
    private Integer progress;
    private Integer sortOrder;
    private Integer childCount;
    private Integer taskCount;
    private Integer deliverableCount;
}
