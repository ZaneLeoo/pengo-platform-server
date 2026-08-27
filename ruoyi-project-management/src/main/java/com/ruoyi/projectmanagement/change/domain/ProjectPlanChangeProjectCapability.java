package com.ruoyi.projectmanagement.change.domain;

import lombok.Data;

/** 当前用户在指定项目中发起计划变更的能力。 */
@Data
public class ProjectPlanChangeProjectCapability {
    private boolean canCreate;
    private String readonlyReason;
}
