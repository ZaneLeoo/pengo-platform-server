package com.ruoyi.projectmanagement.budget.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetSummary;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.budget.service.IProjectWorkPackageBudgetService;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectWorkPackageBudgetServiceImpl implements IProjectWorkPackageBudgetService {
    private final ProjectWorkPackageBudgetMapper mapper;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectBudgetMapper projectBudgetMapper;
    private final ICostCategoryService categoryService;

    public ProjectWorkPackageBudgetServiceImpl(
            ProjectWorkPackageBudgetMapper mapper,
            ProjectWbsMapper wbsMapper,
            ProjectInfoMapper projectMapper,
            ProjectBudgetMapper projectBudgetMapper,
            ICostCategoryService categoryService) {
        this.mapper = mapper;
        this.wbsMapper = wbsMapper;
        this.projectMapper = projectMapper;
        this.projectBudgetMapper = projectBudgetMapper;
        this.categoryService = categoryService;
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

    @Override
    @Transactional
    public void replaceInitial(
            Long projectId, List<ProjectWorkPackageBudgetLine> requested, String operator) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) throw new ServiceException("项目不存在");
        if (!ProjectStatus.APPROVED.matches(project.getStatus())) {
            throw new ServiceException("仅待启动项目可以配置初始工作包预算");
        }
        List<ProjectBudgetLine> projectLines = projectBudgetMapper.selectByProjectId(projectId);
        if (!"1".equals(project.getBudgetRequired()) || projectLines.isEmpty()) {
            throw new ServiceException("项目尚未配置分类预算，不能分配工作包预算");
        }
        List<ProjectWorkPackageBudgetLine> lines = requested == null ? List.of() : requested;
        Map<Long, CostCategory> categories =
                categoryService.options().stream()
                        .collect(Collectors.toMap(CostCategory::getCostCategoryId, x -> x));
        Map<Long, BigDecimal> projectBudget =
                projectLines.stream()
                        .collect(
                                Collectors.toMap(
                                        ProjectBudgetLine::getCostCategoryId,
                                        x -> money(x.getBudgetAmount())));
        ProjectWbsNode filter = new ProjectWbsNode();
        filter.setProjectId(projectId);
        Map<Long, ProjectWbsNode> workPackages =
                wbsMapper.selectList(filter).stream()
                        .filter(x -> "WORK_PACKAGE".equals(x.getNodeType()))
                        .collect(Collectors.toMap(ProjectWbsNode::getWbsId, x -> x));
        Set<String> unique = new HashSet<>();
        Map<Long, BigDecimal> totals = new HashMap<>();
        for (ProjectWorkPackageBudgetLine line : lines) {
            if (line == null
                    || line.getWorkPackageId() == null
                    || line.getCostCategoryId() == null) {
                throw new ServiceException("工作包和成本类别不能为空");
            }
            ProjectWbsNode workPackage = workPackages.get(line.getWorkPackageId());
            if (workPackage == null || "COMPLETED".equals(workPackage.getStatus())) {
                throw new ServiceException("工作包不存在、不是当前项目工作包或已完成");
            }
            CostCategory category = categories.get(line.getCostCategoryId());
            if (category == null) throw new ServiceException("工作包预算成本类别无效或已停用");
            String key = line.getWorkPackageId() + ":" + line.getCostCategoryId();
            if (!unique.add(key)) throw new ServiceException("同一工作包不能重复分配同一成本类别");
            BigDecimal amount = line.getBudgetAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("工作包预算金额必须大于0");
            }
            if (amount.scale() > 2) throw new ServiceException("工作包预算金额最多保留两位小数");
            if (line.getEstimationBasis() == null || line.getEstimationBasis().isBlank()) {
                throw new ServiceException("工作包预算必须填写测算依据");
            }
            line.setProjectId(projectId);
            line.setCategoryCode(category.getCategoryCode());
            line.setCategoryName(category.getCategoryName());
            line.setCategoryPath(category.getFullPath());
            line.setBudgetAmount(amount.setScale(2, RoundingMode.UNNECESSARY));
            line.setEstimationBasis(line.getEstimationBasis().trim());
            totals.merge(line.getCostCategoryId(), line.getBudgetAmount(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : totals.entrySet()) {
            BigDecimal limit = projectBudget.get(entry.getKey());
            if (limit == null || entry.getValue().compareTo(limit) > 0) {
                throw new ServiceException("工作包预算分配不能超过项目分类预算");
            }
        }
        mapper.deleteByProjectId(projectId);
        int sort = 0;
        for (ProjectWorkPackageBudgetLine line : lines) {
            line.setSortOrder(sort++);
            line.setCreateBy(operator);
            if (mapper.insert(line) == 0) throw new ServiceException("保存初始工作包预算失败");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
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
        BigDecimal actual =
                lines.stream()
                        .map(ProjectWorkPackageBudgetLine::getWorkPackageActualAmount)
                        .filter(x -> x != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setActualCostAmount(actual);
        result.setRemainingBudgetAmount(result.getAllocatedAmount().subtract(actual));
        return result;
    }
}
