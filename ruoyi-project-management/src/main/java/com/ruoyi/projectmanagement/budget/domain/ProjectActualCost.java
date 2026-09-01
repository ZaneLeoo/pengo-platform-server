package com.ruoyi.projectmanagement.budget.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目实际成本登记记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectActualCost extends BaseEntity {
    private Long actualCostId;
    private Long projectId;

    /** 为空表示项目级成本；否则必须是当前项目的工作包。 */
    private Long workPackageId;

    private String workPackageCode;
    private String workPackageName;
    private String workPackageStatus;
    private Long costCategoryId;
    private String categoryCode;
    private String categoryName;
    private String categoryPath;
    private String categoryStatus;

    /** 人民币含税实际成本金额，大于 0 且最多保留两位小数。 */
    private BigDecimal actualAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate occurDate;

    private String description;

    /** 更正次数，仅供展示。 */
    private Integer correctionNo;

    /** 成本来源：MANUAL / PURCHASE_INBOUND。 */
    private String sourceType;

    private Long sourceLineId;
    private String sourceDocumentNo;
    private String sourceLineNo;
    private String costStatus;
    private String reverseReason;
    private String reversedBy;
    private java.time.LocalDateTime reversedTime;

    /** 由当前用户和项目角色计算，仅控制前端操作入口。 */
    private Boolean canCorrect;

    /** 删除能力，服务端写入时仍会独立校验。 */
    private Boolean canDelete;
}
