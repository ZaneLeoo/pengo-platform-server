package com.ruoyi.agent.domain.enums;

/** 消息角色。 */
public enum MessageRole
{
    USER("user"), ASSISTANT("assistant");
    private final String code;
    MessageRole(String code) { this.code = code; }
    /** 返回持久化值。 */
    public String getCode() { return code; }
}
