package com.ruoyi.projectmanagement.common.enums;

/** 项目生命周期状态。数据库持久化使用 {@link #getCode()}。 */
public enum ProjectStatus {
    DRAFT("DRAFT"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    APPROVED("APPROVED"),
    ACTIVE("ACTIVE"),
    PAUSED("PAUSED"),
    COMPLETED("COMPLETED");

    private final String code;

    ProjectStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String value) {
        return code.equals(value);
    }
}
