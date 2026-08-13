package com.ruoyi.projectmanagement.common.enums;

/** 交付项当前状态。 */
public enum DeliverableStatus {
    PENDING("PENDING"),
    DELIVERED("DELIVERED"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    APPROVED("APPROVED"),
    RETURNED("RETURNED");

    private final String code;
    DeliverableStatus(String code) { this.code = code; }
    public String getCode() { return code; }
    public boolean matches(String value) { return code.equals(value); }
}
