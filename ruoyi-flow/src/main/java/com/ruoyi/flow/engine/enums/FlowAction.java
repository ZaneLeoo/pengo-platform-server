package com.ruoyi.flow.engine.enums;

/**
 * 审批流转动作（历史记录用）。
 */
public enum FlowAction {
    SUBMIT("SUBMIT"),
    APPROVE("APPROVE"),
    REJECT("REJECT"),
    CANCEL("CANCEL");

    private final String code;

    FlowAction(String code) { this.code = code; }

    public String getCode() { return code; }
}
