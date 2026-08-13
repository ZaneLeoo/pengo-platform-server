package com.ruoyi.projectmanagement.common.enums;

/**
 * WBS节点类型：汇总WBS（SUMMARY）与工作包（WORK_PACKAGE）。
 */
public enum WbsNodeType {
    SUMMARY("SUMMARY"),
    WORK_PACKAGE("WORK_PACKAGE");

    private final String code;

    WbsNodeType(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
