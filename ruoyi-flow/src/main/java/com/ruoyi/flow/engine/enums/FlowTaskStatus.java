package com.ruoyi.flow.engine.enums;

/**
 * 待办任务状态。
 */
public enum FlowTaskStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    SKIPPED("SKIPPED");

    private final String code;

    FlowTaskStatus(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
