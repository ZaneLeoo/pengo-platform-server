package com.ruoyi.projectmanagement.budget.service;

import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetSummary;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import java.util.List;

public interface IProjectBudgetService {
    List<ProjectBudgetLine> lines(Long projectId);

    ProjectBudgetSummary summary(Long projectId);

    void replaceDraft(ProjectInfo project, List<ProjectBudgetLine> lines, String operator);

    void validateForSubmission(ProjectInfo project, List<ProjectBudgetLine> lines);
}
