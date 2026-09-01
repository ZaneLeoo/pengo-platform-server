package com.ruoyi.projectmanagement.budget.service;

import com.ruoyi.projectmanagement.budget.domain.ProjectActualCost;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetExecutionSummary;
import java.util.List;

/** 项目实际成本登记与预算执行查询。 */
public interface IProjectActualCostService {
    List<ProjectActualCost> list(Long projectId, Long userId);

    ProjectActualCost register(
            Long projectId, ProjectActualCost cost, String operator, Long userId);

    ProjectActualCost correct(
            Long projectId, Long costId, ProjectActualCost patch, String operator, Long userId);

    void delete(Long projectId, Long costId, String operator, Long userId);

    ProjectBudgetExecutionSummary execution(Long projectId, Long userId);
}
