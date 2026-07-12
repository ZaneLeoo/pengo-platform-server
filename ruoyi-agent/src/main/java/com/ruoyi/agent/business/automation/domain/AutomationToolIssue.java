package com.ruoyi.agent.business.automation.domain;

/** AI 工具需要用户或 Agent 修正的结构化问题。 */
public record AutomationToolIssue(String code, String field, String message)
{
}
