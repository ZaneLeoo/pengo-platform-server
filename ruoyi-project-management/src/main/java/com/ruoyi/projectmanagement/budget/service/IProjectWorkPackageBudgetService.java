package com.ruoyi.projectmanagement.budget.service;

import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetSummary;
import java.util.List;

public interface IProjectWorkPackageBudgetService {
    ProjectWorkPackageBudgetSummary projectSummary(Long projectId);

    ProjectWorkPackageBudgetSummary workPackageSummary(Long projectId, Long workPackageId);

    /** 待启动项目配置初始工作包预算。 */
    void replaceInitial(Long projectId, List<ProjectWorkPackageBudgetLine> lines, String operator);
}
