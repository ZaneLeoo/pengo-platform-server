package com.ruoyi.agent.domain.enums;

/** 会话状态。 */
public enum ConversationStatus
{
    ACTIVE("active"), DELETED("deleted");
    private final String code;
    ConversationStatus(String code) { this.code = code; }
    /** 返回持久化值。 */
    public String getCode() { return code; }
}
