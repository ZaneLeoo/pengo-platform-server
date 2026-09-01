package com.ruoyi.projectmanagement.budget.service;

import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetSummary;

public interface IProjectWorkPackageBudgetService {
    ProjectWorkPackageBudgetSummary projectSummary(Long projectId);

    ProjectWorkPackageBudgetSummary workPackageSummary(Long projectId, Long workPackageId);
}
