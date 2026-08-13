package com.ruoyi.projectmanagement.common.enums;

/** WBS 任务生命周期状态。 */
public enum WorkItemStatus {
    NOT_STARTED("NOT_STARTED"),
    ACTIVE("ACTIVE"),
    PAUSED("PAUSED"),
    COMPLETED("COMPLETED");

    private final String code;
    WorkItemStatus(String code) { this.code = code; }
    public String getCode() { return code; }
    public boolean matches(String value) { return code.equals(value); }
}
