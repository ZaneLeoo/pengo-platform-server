package com.ruoyi.projectmanagement.common.enums;

/**
 * WBS与工作包汇总状态。
 */
public enum WbsStatus {
    NOT_STARTED("NOT_STARTED"),
    ACTIVE("ACTIVE"),
    WAITING_DELIVERY("WAITING_DELIVERY"),
    COMPLETED("COMPLETED");

    private final String code;

    WbsStatus(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
