package com.ruoyi.flow.definition.enums;

/**
 * 节点审批方式：OR或签（任一同意即通过），AND会签（全部同意才通过）。
 */
public enum FlowSignType {
    OR("OR"),
    AND("AND");

    private final String code;

    FlowSignType(String code) { this.code = code; }

    public String getCode() { return code; }

    public boolean matches(String value) { return code.equalsIgnoreCase(value); }
}
