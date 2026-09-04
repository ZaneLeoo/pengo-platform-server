package com.ruoyi.projectmanagement.workhours.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 全局人员小时单价。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectLaborRate extends BaseEntity {
    private Long rateId;
    private Long userId;
    private String userName;
    private String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveEndDate;

    private BigDecimal hourlyRate;
    private String status;
}
