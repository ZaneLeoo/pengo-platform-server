package com.ruoyi.agent.tool.shared;

import java.util.List;

/** Agent 工具需要用户或模型修正的结构化问题。 */
public record AgentToolIssue(
        String code,
        String field,
        String message,
        String expected,
        List<AgentToolCandidate> candidates) {
    /** 规范问题字段并确保候选列表不可变。 */
    public AgentToolIssue {
        code = code == null ? "" : code;
        field = field == null ? "" : field;
        message = message == null ? "" : message;
        expected = expected == null ? "" : expected;
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    /** 创建不包含候选项的普通问题。 */
    public static AgentToolIssue of(String code, String field, String message, String expected) {
        return new AgentToolIssue(code, field, message, expected, List.of());
    }
}
