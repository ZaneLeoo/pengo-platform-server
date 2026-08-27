package com.ruoyi.projectmanagement.change.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目计划不可变基线快照。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPlanBaseline extends BaseEntity {
    private Long baselineId;
    private Long projectId;
    private Integer versionNo;
    private Long sourceChangeId;
    private String snapshotJson;
}
