package com.ruoyi.projectmanagement.common.enums;

/** 项目成员在组状态。 */
public enum ProjectMemberStatus {
    ACTIVE("ACTIVE"),
    EXITED("EXITED");

    private final String code;

    ProjectMemberStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String value) {
        return code.equals(value);
    }
}
