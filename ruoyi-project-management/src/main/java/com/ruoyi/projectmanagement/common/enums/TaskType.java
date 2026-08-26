package com.ruoyi.projectmanagement.common.enums;

/** 任务类型：汇总任务（SUMMARY）与执行任务（EXECUTION）。 */
public enum TaskType {
    SUMMARY("SUMMARY"),
    EXECUTION("EXECUTION");

    private final String code;

    TaskType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String value) {
        return code.equals(value);
    }
}
