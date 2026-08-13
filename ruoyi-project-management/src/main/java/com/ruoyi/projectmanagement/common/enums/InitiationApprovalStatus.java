package com.ruoyi.projectmanagement.common.enums;

/** 立项申请审批记录状态。 */
public enum InitiationApprovalStatus {
    PENDING("PENDING"), APPROVED("APPROVED"), RETURNED("RETURNED");

    private final String code;
    InitiationApprovalStatus(String code) { this.code = code; }
    public String getCode() { return code; }
    public boolean matches(String value) { return code.equals(value); }
}
