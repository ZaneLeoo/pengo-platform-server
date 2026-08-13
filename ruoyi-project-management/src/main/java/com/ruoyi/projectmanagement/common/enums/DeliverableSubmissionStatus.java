package com.ruoyi.projectmanagement.common.enums;

/** 交付物提交版本的审核结论。 */
public enum DeliverableSubmissionStatus {
    SUBMITTED("SUBMITTED"), DELIVERED("DELIVERED"), APPROVED("APPROVED"), RETURNED("RETURNED");

    private final String code;
    DeliverableSubmissionStatus(String code) { this.code = code; }
    public String getCode() { return code; }
}
