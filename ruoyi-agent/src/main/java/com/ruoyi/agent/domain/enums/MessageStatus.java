package com.ruoyi.agent.domain.enums;

/** 消息生成状态。 */
public enum MessageStatus
{
    STREAMING("streaming"), COMPLETED("completed"), STOPPED("stopped"), FAILED("failed");
    private final String code;
    MessageStatus(String code) { this.code = code; }
    /** 返回持久化值。 */
    public String getCode() { return code; }
}
