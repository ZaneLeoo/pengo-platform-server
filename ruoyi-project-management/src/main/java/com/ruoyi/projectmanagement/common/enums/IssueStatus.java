package com.ruoyi.projectmanagement.common.enums;

/**
 * 问题跟踪状态。
 */
public enum IssueStatus {
    OPEN("OPEN"),
    PROCESSING("PROCESSING"),
    RESOLVED("RESOLVED"),
    CLOSED("CLOSED");

    private final String code;

    IssueStatus(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
