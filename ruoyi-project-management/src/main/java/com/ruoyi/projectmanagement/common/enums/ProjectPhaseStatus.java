package com.ruoyi.projectmanagement.common.enums;

/** 正式项目阶段生命周期状态。 */
public enum ProjectPhaseStatus {
    NOT_STARTED("NOT_STARTED"),
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED");

    private final String code;
    ProjectPhaseStatus(String code) { this.code = code; }
    public String getCode() { return code; }
    public boolean matches(String value) { return code.equals(value); }
}
