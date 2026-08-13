package com.ruoyi.projectmanagement.common.enums;

/** WBS节点类型。 */
public enum WbsNodeType {
    SUMMARY, WORK_PACKAGE;
    public boolean matches(String value) { return name().equals(value); }
}
