package com.ruoyi.agent.tool.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Agent 工具需要用户或模型修正的结构化问题。 */
@Data
@NoArgsConstructor
public class AgentToolIssue {

    private String code;
    private String field;
    private String message;
    private String expected;
    private List<AgentToolCandidate> candidates;

    /** 规范问题字段并确保候选列表不可变。 */
    public AgentToolIssue(String code, String field, String message, String expected,
            List<AgentToolCandidate> candidates) {
        this.code = code == null ? "" : code;
        this.field = field == null ? "" : field;
        this.message = message == null ? "" : message;
        this.expected = expected == null ? "" : expected;
        this.candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    /** 创建不包含候选项的普通问题。 */
    public static AgentToolIssue of(String code, String field, String message, String expected) {
        return new AgentToolIssue(code, field, message, expected, List.of());
    }
}
