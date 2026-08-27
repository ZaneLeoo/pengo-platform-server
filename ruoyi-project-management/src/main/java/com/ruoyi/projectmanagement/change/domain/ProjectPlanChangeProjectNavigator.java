package com.ruoyi.projectmanagement.change.domain;

import lombok.Data;

/** 当前用户可查看的项目变更导航项。 */
@Data
public class ProjectPlanChangeProjectNavigator {
    /** 项目ID。 */
    private Long projectId;

    /** 项目编码。 */
    private String projectCode;

    /** 项目名称。 */
    private String projectName;

    /** 项目负责人。 */
    private String managerName;

    /** 项目状态。 */
    private String status;

    /** 当前计划基线版本；尚未启动的项目为空。 */
    private Integer currentBaselineVersion;

    /** 尚未结束的变更单数量。 */
    private Integer openChangeCount;
}
