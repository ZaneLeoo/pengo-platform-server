package com.ruoyi.agent.business.automation.domain;

/** AI 工具执行后的下一步动作约定。 */
public enum AutomationToolNextAction
{
    /** 直接向用户追问或要求修正，不得以相同参数重试工具。 */
    ASK_USER,

    /** 向用户展示候选项并等待明确选择。 */
    SELECT_CANDIDATE,

    /** 在聊天区展示标准化草稿，并等待前端确认创建。 */
    SHOW_DRAFT
}
