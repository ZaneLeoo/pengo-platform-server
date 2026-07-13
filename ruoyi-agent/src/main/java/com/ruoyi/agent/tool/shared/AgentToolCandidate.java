package com.ruoyi.agent.tool.shared;

/** Agent 工具要求用户选择时使用的通用候选项。 */
public record AgentToolCandidate(String value, String label, String description) {
    /** 规范空字段，避免工具结果中出现不稳定值。 */
    public AgentToolCandidate {
        value = value == null ? "" : value;
        label = label == null ? "" : label;
        description = description == null ? "" : description;
    }
}
