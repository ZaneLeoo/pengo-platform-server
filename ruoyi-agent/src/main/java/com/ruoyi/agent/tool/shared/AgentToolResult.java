package com.ruoyi.agent.tool.shared;

import java.util.List;

/**
 * Agent 工具统一响应。
 *
 * @param status 工具业务状态
 * @param resultCode 稳定结果码
 * @param message 面向用户的简短说明
 * @param nextAction Agent 下一步动作
 * @param retryable 是否允许使用完全相同的参数立即重试
 * @param agentInstruction 面向 Agent 的行为指令
 * @param issues 结构化问题
 * @param data 强类型业务数据
 * @param meta 数量与分页元数据
 */
public record AgentToolResult<T>(
    AgentToolStatus status,
    String resultCode,
    String message,
    AgentToolNextAction nextAction,
    boolean retryable,
    String agentInstruction,
    List<AgentToolIssue> issues,
    T data,
    AgentToolMeta meta)
{
    /** 规范可空字段并确保问题列表不可变。 */
    public AgentToolResult
    {
        resultCode = resultCode == null ? "" : resultCode;
        message = message == null ? "" : message;
        agentInstruction = agentInstruction == null ? "" : agentInstruction;
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
