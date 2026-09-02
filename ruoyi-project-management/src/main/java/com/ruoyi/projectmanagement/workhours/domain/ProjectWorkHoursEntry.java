package com.ruoyi.projectmanagement.workhours.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目工时日明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWorkHoursEntry extends BaseEntity {
    private Long entryId;
    private Long sheetId;
    private Long projectId;
    private Long workPackageId;
    private Long taskId;
    private Long reportUserId;
    private String projectName;
    private String workPackageName;
    private String taskName;
    private String reportUserName;
    private String reportNickName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    private BigDecimal hours;
    private String overtimeFlag;
    private String workDescription;
    private String achievementDescription;
    private Long sourceEntryId;
    private String correctionReason;
    private String entryStatus;
    private String costStatus;
    private Long rateIdSnapshot;
    private BigDecimal rateAmountSnapshot;
    private Long actualCostId;
}
