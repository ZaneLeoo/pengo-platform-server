package com.ruoyi.projectmanagement.budget.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetSummary;
import com.ruoyi.projectmanagement.budget.mapper.ProjectActualCostMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.service.IProjectBudgetService;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanBaseline;
import com.ruoyi.projectmanagement.change.mapper.ProjectPlanChangeMapper;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class ProjectBudgetServiceImpl implements IProjectBudgetService {
    private final ProjectBudgetMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ICostCategoryService categoryService;
    private final ProjectPlanChangeMapper changeMapper;
    private final ObjectMapper objectMapper;
    private final ProjectActualCostMapper actualCostMapper;

    public ProjectBudgetServiceImpl(
            ProjectBudgetMapper mapper,
            ProjectInfoMapper projectMapper,
            ICostCategoryService categoryService,
            ProjectPlanChangeMapper changeMapper,
            ObjectMapper objectMapper,
            ProjectActualCostMapper actualCostMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.categoryService = categoryService;
        this.changeMapper = changeMapper;
        this.objectMapper = objectMapper;
        this.actualCostMapper = actualCostMapper;
    }

    @Override
    public List<ProjectBudgetLine> lines(Long projectId) {
        return mapper.selectByProjectId(projectId);
    }

    @Override
    public ProjectBudgetSummary summary(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) throw new ServiceException("项目不存在");
        List<ProjectBudgetLine> lines = lines(projectId);
        BigDecimal allocated = total(lines);
        BigDecimal total = money(project.getBudgetAmount());
        ProjectBudgetSummary result = new ProjectBudgetSummary();
        result.setProjectId(projectId);
        result.setBudgetRequired(project.getBudgetRequired());
        result.setBudgetAmount(project.getBudgetAmount());
        result.setBudgetDescription(project.getBudgetDescription());
        result.setAllocatedAmount(allocated);
        result.setDifferenceAmount(total.subtract(allocated));
        BigDecimal initial = initialApprovedAmount(projectId, total);
        result.setInitialApprovedAmount(initial);
        result.setCumulativeChangeAmount(total.subtract(initial));
        result.setCategoryCount(lines.size());
        result.setLines(lines);
        BigDecimal actual = actualCostMapper.totalByProjectId(projectId);
        result.setActualCostAmount(actual == null ? BigDecimal.ZERO : actual);
        result.setRemainingBudgetAmount(total.subtract(result.getActualCostAmount()));
        result.setExecutionRate(
                total.signum() > 0
                        ? result.getActualCostAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
        return result;
    }

    @Override
    @Transactional
    public void replaceDraft(
            ProjectInfo project, List<ProjectBudgetLine> requested, String operator) {
        List<ProjectBudgetLine> lines = requested == null ? List.of() : requested;
        validateLines(lines, false);
        if (!"1".equals(project.getBudgetRequired())) {
            if (project.getBudgetAmount() != null
                    || (project.getBudgetDescription() != null
                            && !project.getBudgetDescription().isBlank())
                    || !lines.isEmpty()) {
                throw new ServiceException("不需要预算时预算总额、预算说明和分类明细必须为空");
            }
        }
        mapper.deleteByProjectId(project.getProjectId());
        if (!"1".equals(project.getBudgetRequired())) return;
        Map<Long, CostCategory> options = new HashMap<>();
        categoryService.options().forEach(item -> options.put(item.getCostCategoryId(), item));
        int sort = 0;
        for (ProjectBudgetLine line : lines) {
            CostCategory category = options.get(line.getCostCategoryId());
            if (category == null) throw new ServiceException("预算明细包含无效或已停用成本类别");
            line.setProjectId(project.getProjectId());
            line.setCategoryCode(category.getCategoryCode());
            line.setCategoryName(category.getCategoryName());
            line.setCategoryPath(category.getFullPath());
            line.setBudgetAmount(line.getBudgetAmount().setScale(2, RoundingMode.UNNECESSARY));
            line.setEstimationBasis(line.getEstimationBasis().trim());
            line.setSortOrder(sort++);
            line.setCreateBy(operator);
            mapper.insert(line);
        }
    }

    @Override
    public void validateForSubmission(ProjectInfo project, List<ProjectBudgetLine> lines) {
        if (!"1".equals(project.getBudgetRequired())) {
            if (project.getBudgetAmount() != null || (lines != null && !lines.isEmpty()))
                throw new ServiceException("不需要预算时不能填写预算总额或分类明细");
            return;
        }
        if (project.getBudgetAmount() == null
                || project.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new ServiceException("项目预算总额必须大于0");
        if (lines == null || lines.isEmpty()) throw new ServiceException("请至少填写一条分类预算明细");
        validateLines(lines, true);
        if (money(project.getBudgetAmount()).compareTo(total(lines)) != 0)
            throw new ServiceException("分类预算合计必须等于预算总额");
    }

    private void validateLines(List<ProjectBudgetLine> lines, boolean requireActive) {
        Set<Long> categories = new HashSet<>();
        Set<Long> active = new HashSet<>();
        if (requireActive)
            categoryService.options().forEach(item -> active.add(item.getCostCategoryId()));
        for (ProjectBudgetLine line : lines) {
            if (line == null || line.getCostCategoryId() == null)
                throw new ServiceException("成本类别不能为空");
            if (!categories.add(line.getCostCategoryId()))
                throw new ServiceException("同一成本类别不能重复编制预算");
            if (line.getBudgetAmount() == null
                    || line.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0)
                throw new ServiceException("分类预算金额必须大于0");
            if (line.getBudgetAmount().scale() > 2) throw new ServiceException("分类预算金额最多保留两位小数");
            if (line.getEstimationBasis() == null || line.getEstimationBasis().isBlank())
                throw new ServiceException("分类预算必须填写测算依据");
            if (requireActive && !active.contains(line.getCostCategoryId()))
                throw new ServiceException("预算明细包含无效或已停用成本类别");
        }
    }

    private BigDecimal total(List<ProjectBudgetLine> lines) {
        return lines.stream()
                .map(ProjectBudgetLine::getBudgetAmount)
                .filter(x -> x != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal initialApprovedAmount(Long projectId, BigDecimal fallback) {
        List<ProjectPlanBaseline> baselines = changeMapper.selectBaselines(projectId);
        if (baselines.isEmpty() || baselines.get(baselines.size() - 1).getSnapshotJson() == null)
            return fallback;
        try {
            Map<String, Object> snapshot =
                    objectMapper.readValue(
                            baselines.get(baselines.size() - 1).getSnapshotJson(),
                            new TypeReference<Map<String, Object>>() {});
            Object rawProject = snapshot.get("project");
            if (rawProject instanceof Map<?, ?> project) {
                Object amount = project.get("budgetAmount");
                if (amount != null) return money(new BigDecimal(String.valueOf(amount)));
            }
        } catch (Exception ignored) {
            // 历史基线可能形成于分类预算上线之前，回退为当前值即可。
        }
        return fallback;
    }
}
