package com.ruoyi.projectmanagement.common.enums;

/** 项目或WBS任务的生命周期动作。 */
public enum LifecycleAction {
    START("START"),
    PAUSE("PAUSE"),
    RESUME("RESUME"),
    COMPLETE("COMPLETE");

    private final String code;

    LifecycleAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String value) {
        return code.equals(value);
    }

    /** 按编码解析动作，未知值返回 null。 */
    public static LifecycleAction fromCode(String value) {
        if (value == null) return null;
        for (LifecycleAction action : values()) {
            if (action.code.equalsIgnoreCase(value.trim())) return action;
        }
        return null;
    }
}
