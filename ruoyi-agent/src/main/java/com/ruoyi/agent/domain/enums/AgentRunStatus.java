package com.ruoyi.agent.domain.enums;

/** Agent V2 一次运行的生命周期状态。 */
public enum AgentRunStatus
{
    CREATED,
    RUNNING,
    WAITING_CONFIRMATION,
    COMPLETED,
    FAILED,
    CANCELLED;

    /** 判断状态是否已经结束。 */
    public boolean isTerminal()
    {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
