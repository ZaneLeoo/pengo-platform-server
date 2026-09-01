package com.ruoyi.projectmanagement.budget.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetSummary;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.budget.service.IProjectWorkPackageBudgetService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectWorkPackageBudgetServiceImpl implements IProjectWorkPackageBudgetService {
    private final ProjectWorkPackageBudgetMapper mapper;
    private final ProjectWbsMapper wbsMapper;

    public ProjectWorkPackageBudgetServiceImpl(
            ProjectWorkPackageBudgetMapper mapper, ProjectWbsMapper wbsMapper) {
        this.mapper = mapper;
        this.wbsMapper = wbsMapper;
    }

    @Override
    public ProjectWorkPackageBudgetSummary projectSummary(Long projectId) {
        return summary(projectId, null, mapper.selectByProjectId(projectId));
    }

    @Override
    public ProjectWorkPackageBudgetSummary workPackageSummary(Long projectId, Long workPackageId) {
        ProjectWbsNode workPackage = wbsMapper.selectById(workPackageId);
        if (workPackage == null
                || !projectId.equals(workPackage.getProjectId())
                || !"WORK_PACKAGE".equals(workPackage.getNodeType())) {
            throw new ServiceException("工作包不存在或不属于当前项目");
        }
        return summary(projectId, workPackageId, mapper.selectByWorkPackageId(workPackageId));
    }

    private ProjectWorkPackageBudgetSummary summary(
            Long projectId, Long workPackageId, List<ProjectWorkPackageBudgetLine> lines) {
        ProjectWorkPackageBudgetSummary result = new ProjectWorkPackageBudgetSummary();
        result.setProjectId(projectId);
        result.setWorkPackageId(workPackageId);
        result.setLines(lines);
        result.setCategoryCount(lines.size());
        result.setAllocatedAmount(
                lines.stream()
                        .map(ProjectWorkPackageBudgetLine::getBudgetAmount)
                        .filter(x -> x != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }
}
