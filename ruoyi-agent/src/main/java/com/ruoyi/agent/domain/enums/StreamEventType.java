package com.ruoyi.agent.domain.enums;

/** RuoYi 向前端输出的 SSE 事件类型。 */
public enum StreamEventType
{
    CONVERSATION("conversation"), MESSAGE("message"), MESSAGE_REPLACE("message_replace"),
    WORKFLOW("workflow"), NODE("node"), ARTIFACT("artifact"), SOURCES("sources"), ERROR("error"), DONE("done");
    private final String code;
    StreamEventType(String code) { this.code = code; }
    /** 返回 SSE event 名称。 */
    public String getCode() { return code; }
}
