package com.ruoyi.agent.business.automation.domain;

import java.util.List;

/** 需要用户消歧的主数据候选项。 */
public record AutomationCandidate(String field, String keyword, List<AutomationCandidateOption> options)
{
}
