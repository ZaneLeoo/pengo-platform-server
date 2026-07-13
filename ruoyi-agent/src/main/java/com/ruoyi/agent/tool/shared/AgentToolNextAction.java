package com.ruoyi.agent.tool.shared;

/** Agent 收到工具结果后必须采取的下一步动作。 */
public enum AgentToolNextAction
{
    CONTINUE,
    PRESENT_RESULT,
    ASK_USER,
    SELECT_CANDIDATE,
    CONFIRM_ACTION,
    STOP
}
