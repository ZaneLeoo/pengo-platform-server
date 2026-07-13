package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 需要用户消歧的主数据候选项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationCandidate {
    private String field;
    private String keyword;
    private List<AutomationCandidateOption> options;

}
