package com.ruoyi.projectmanagement.change.domain;

import lombok.Data;

/** 变更单中的逐项计划调整。before/after 使用 JSON，避免审批数据随当前计划漂移。 */
@Data
public class ProjectPlanChangeItem {
    private Long itemId;
    private Long changeId;
    private String moduleType;
    private String operationType;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String beforeJson;
    private String afterJson;
    private String itemReason;
    private Integer sortOrder;
}
