package com.ruoyi.projectmanagement.common.enums;

/** 项目执行项类别。 */
public enum WorkItemType {
    TASK("TASK"),
    DELIVERABLE("DELIVERABLE"),
    ISSUE("ISSUE");

    private final String code;
    WorkItemType(String code) { this.code = code; }
    public String getCode() { return code; }
    public boolean matches(String value) { return code.equals(value); }
}
