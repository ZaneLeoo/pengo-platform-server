package com.ruoyi.agent.business.automation.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 需要用户消歧的主数据候选项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationCandidate {
    private String field;
    private String keyword;
    private List<AutomationCandidateOption> options;
}
