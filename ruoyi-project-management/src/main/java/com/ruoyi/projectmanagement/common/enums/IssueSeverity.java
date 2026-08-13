package com.ruoyi.projectmanagement.common.enums;

/**
 * 问题严重程度。
 */
public enum IssueSeverity {
    HIGH("HIGH"),
    MEDIUM("MEDIUM"),
    LOW("LOW");

    private final String code;

    IssueSeverity(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
