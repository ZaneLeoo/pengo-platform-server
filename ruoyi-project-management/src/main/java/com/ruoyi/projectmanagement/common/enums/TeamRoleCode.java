package com.ruoyi.projectmanagement.common.enums;

/**
 * 项目团队系统预置角色编码。
 */
public enum TeamRoleCode {
    PROJECT_MANAGER("PROJECT_MANAGER"),
    CORE_MEMBER("CORE_MEMBER"),
    MEMBER("MEMBER");

    private final String code;

    TeamRoleCode(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equals(value); }
}
