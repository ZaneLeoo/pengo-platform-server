package com.ruoyi.flow.engine.enums;

/**
 * 流程实例状态。
 */
public enum FlowInstanceStatus {
    RUNNING("RUNNING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    private final String code;

    FlowInstanceStatus(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
