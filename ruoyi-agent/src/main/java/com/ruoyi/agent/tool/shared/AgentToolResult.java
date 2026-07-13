package com.ruoyi.agent.tool.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent 工具统一响应。
 */
@Data
@NoArgsConstructor
public class AgentToolResult<T> {
    private AgentToolStatus status;
    private String resultCode;
    private String message;
    private AgentToolNextAction nextAction;
    private boolean retryable;
    private String agentInstruction;
    private List<AgentToolIssue> issues;
    private T data;
    private AgentToolMeta meta;

    /** 规范可空字段并确保问题列表不可变。 */
    public AgentToolResult(AgentToolStatus status, String resultCode, String message, AgentToolNextAction nextAction,
            boolean retryable, String agentInstruction, List<AgentToolIssue> issues, T data, AgentToolMeta meta) {
        this.status = status;
        this.resultCode = resultCode == null ? "" : resultCode;
        this.message = message == null ? "" : message;
        this.nextAction = nextAction;
        this.retryable = retryable;
        this.agentInstruction = agentInstruction == null ? "" : agentInstruction;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.data = data;
        this.meta = meta;
    }
}
