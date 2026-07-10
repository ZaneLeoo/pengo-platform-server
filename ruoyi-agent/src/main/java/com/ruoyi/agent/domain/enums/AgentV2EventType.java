package com.ruoyi.agent.domain.enums;

/** Spring 向前端发布的 Agent V2 SSE 事件。 */
public enum AgentV2EventType
{
    RUN_CREATED("run.created"),
    MESSAGE_DELTA("message.delta"),
    MESSAGE_REPLACED("message.replaced"),
    STEP_STARTED("step.started"),
    STEP_COMPLETED("step.completed"),
    STEP_FAILED("step.failed"),
    TOOL_STARTED("tool.started"),
    TOOL_COMPLETED("tool.completed"),
    TOOL_FAILED("tool.failed"),
    ARTIFACT_CREATED("artifact.created"),
    CITATION_CREATED("citation.created"),
    APPROVAL_REQUIRED("approval.required"),
    RUN_COMPLETED("run.completed"),
    RUN_FAILED("run.failed"),
    RUN_CANCELLED("run.cancelled");

    private final String code;

    AgentV2EventType(String code)
    {
        this.code = code;
    }

    /** 返回 SSE event 名称。 */
    public String getCode()
    {
        return code;
    }
}
