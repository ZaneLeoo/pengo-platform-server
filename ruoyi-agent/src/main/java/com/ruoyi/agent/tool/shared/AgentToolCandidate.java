package com.ruoyi.agent.tool.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Agent 工具要求用户选择时使用的通用候选项。 */
@Data
@NoArgsConstructor
public class AgentToolCandidate {
    private String value;
    private String label;
    private String description;

    /** 规范空字段，避免工具结果中出现不稳定值。 */
    public AgentToolCandidate(String value, String label, String description) {
        this.value = value == null ? "" : value;
        this.label = label == null ? "" : label;
        this.description = description == null ? "" : description;
    }
}
