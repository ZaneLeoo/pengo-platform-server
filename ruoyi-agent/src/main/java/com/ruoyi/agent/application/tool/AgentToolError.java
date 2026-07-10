package com.ruoyi.agent.application.tool;

/** 工具失败时返回给 Supervisor 的受控错误。 */
public record AgentToolError(String code, String message, boolean retryable)
{
}
