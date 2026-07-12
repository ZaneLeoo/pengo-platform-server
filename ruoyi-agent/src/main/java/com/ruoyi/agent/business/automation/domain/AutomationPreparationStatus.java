package com.ruoyi.agent.business.automation.domain;

/**
 * 自动化动作准备阶段的结果状态。
 *
 * <p>该状态只描述“是否已经具备执行条件”，不代表任何业务单据已经创建。</p>
 */
public enum AutomationPreparationStatus {

    /** 仍缺少业务必填信息，需要助手继续向用户确认。 */
    NEED_INPUT,

    /** 主数据存在多个候选项，需要用户消除歧义。 */
    AMBIGUOUS,

    /** 主数据或业务规则校验未通过，当前不能继续执行。 */
    INVALID,

    /** 已形成可供用户确认的标准化草稿。 */
    READY
}
