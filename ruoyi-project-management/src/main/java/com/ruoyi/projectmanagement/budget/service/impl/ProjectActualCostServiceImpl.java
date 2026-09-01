package com.ruoyi.projectmanagement.budget.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCost;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCostAggregate;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetExecutionRow;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetExecutionSummary;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.mapper.ProjectActualCostMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.budget.service.IProjectActualCostService;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 实际成本登记、更正、删除与预算执行统计。 */
@Service
public class ProjectActualCostServiceImpl implements IProjectActualCostService {
    private final ProjectActualCostMapper mapper;
    private final ProjectBudgetMapper budgetMapper;
    private final ProjectWorkPackageBudgetMapper workPackageBudgetMapper;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectInfoMapper projectMapper;
    private final ICostCategoryService costCategoryService;
    private final IProjectTeamService teamService;

    public ProjectActualCostServiceImpl(
            ProjectActualCostMapper mapper,
            ProjectBudgetMapper budgetMapper,
            ProjectWorkPackageBudgetMapper workPackageBudgetMapper,
            ProjectWbsMapper wbsMapper,
            ProjectInfoMapper projectMapper,
            ICostCategoryService costCategoryService,
            IProjectTeamService teamService) {
        this.mapper = mapper;
        this.budgetMapper = budgetMapper;
        this.workPackageBudgetMapper = workPackageBudgetMapper;
        this.wbsMapper = wbsMapper;
        this.projectMapper = projectMapper;
        this.costCategoryService = costCategoryService;
        this.teamService = teamService;
    }

    @Override
    public List<ProjectActualCost> list(Long projectId, Long userId) {
        assertViewable(projectId, userId);
        ProjectInfo project = requireProject(projectId);
        boolean editable = isEditableStatus(project);
        String username = SecurityUtils.getUsername();
        List<ProjectActualCost> costs = mapper.selectByProjectId(projectId);
        costs.forEach(
                cost -> {
                    boolean owned =
                            isManager(project, userId)
                                    || Objects.equals(username, cost.getCreateBy());
                    boolean manual = "MANUAL".equals(cost.getSourceType());
                    cost.setCanCorrect(editable && owned && manual);
                    cost.setCanDelete(editable && owned && manual);
                });
        return costs;
    }

    @Override
    @Transactional
    public ProjectActualCost register(
            Long projectId, ProjectActualCost cost, String operator, Long userId) {
        assertEditable(projectId, userId);
        requireCategoryAndBudget(projectId, cost.getCostCategoryId());
        ProjectActualCost prepared = prepare(projectId, cost, operator);
        Long workPackageId = prepared.getWorkPackageId();
        if (workPackageId != null) requireWorkPackageBudget(projectId, workPackageId, prepared);
        BigDecimal projectBudget =
                requireBudgetLine(projectId, prepared.getCostCategoryId()).getBudgetAmount();
        BigDecimal after =
                mapper.categoryTotal(projectId, prepared.getCostCategoryId())
                        .add(prepared.getActualAmount());
        if (after.compareTo(projectBudget) > 0) throw new ServiceException("实际成本合计不能超过项目分类预算");
        if (workPackageId != null) {
            BigDecimal workPackageBudget =
                    workPackageBudget(workPackageId, prepared.getCostCategoryId())
                            .getBudgetAmount();
            BigDecimal workPackageAfter =
                    mapper.workPackageCategoryTotal(
                                    projectId, workPackageId, prepared.getCostCategoryId())
                            .add(prepared.getActualAmount());
            if (workPackageAfter.compareTo(workPackageBudget) > 0)
                throw new ServiceException("实际成本合计不能超过该工作包预算");
        }
        mapper.insert(prepared);
        return mapper.selectById(prepared.getActualCostId());
    }

    @Override
    @Transactional
    public ProjectActualCost correct(
            Long projectId, Long costId, ProjectActualCost patch, String operator, Long userId) {
        ProjectActualCost old = require(projectId, costId);
        if (!"MANUAL".equals(old.getSourceType()))
            throw new ServiceException("采购入库来源的实际成本请通过采购入库弃审冲销");
        assertEditable(projectId, userId);
        assertOwned(old, operator, userId);
        if (patch.getCostCategoryId() != null
                && !patch.getCostCategoryId().equals(old.getCostCategoryId()))
            throw new ServiceException("实际成本不允许修改成本类别");
        assertEditableStatus(projectId);
        BigDecimal amount = amount(patch.getActualAmount(), "实际成本金额");
        LocalDate date = requireDate(patch.getOccurDate());
        String description = requireText(patch.getDescription());
        Long workPackageId = patch.getWorkPackageId();
        // 更新请求传 0 明确表示“移回项目级”；null 仍表示未修改，兼容旧客户端。
        if (workPackageId == null) {
            workPackageId = old.getWorkPackageId();
        } else if (workPackageId == 0L) {
            workPackageId = null;
        }
        validateWorkPackageChange(projectId, workPackageId);
        boolean categoryActive =
                costCategoryService.options().stream()
                        .anyMatch(item -> item.getCostCategoryId().equals(old.getCostCategoryId()));
        if (!categoryActive) {
            if (amount.compareTo(old.getActualAmount()) >= 0)
                throw new ServiceException("停用成本类别仅允许调减或删除实际成本");
            requireBudgetLine(projectId, old.getCostCategoryId());
        } else {
            requireCategoryAndBudget(projectId, old.getCostCategoryId());
        }
        if (workPackageId != null) requireWorkPackageBudget(projectId, workPackageId, old);
        BigDecimal oldCategoryTotal = mapper.categoryTotal(projectId, old.getCostCategoryId());
        BigDecimal oldWorkPackageTotal =
                old.getWorkPackageId() == null
                        ? BigDecimal.ZERO
                        : mapper.workPackageCategoryTotal(
                                projectId, old.getWorkPackageId(), old.getCostCategoryId());
        BigDecimal newCategoryTotal = oldCategoryTotal.subtract(old.getActualAmount()).add(amount);
        BigDecimal newWorkPackageTotal =
                workPackageId == null
                        ? BigDecimal.ZERO
                        : sameWorkPackage(old, workPackageId)
                                ? oldWorkPackageTotal.subtract(old.getActualAmount()).add(amount)
                                : mapper.workPackageCategoryTotal(
                                                projectId, workPackageId, old.getCostCategoryId())
                                        .add(amount);
        BigDecimal projectBudget =
                requireBudgetLine(projectId, old.getCostCategoryId()).getBudgetAmount();
        if (newCategoryTotal.compareTo(projectBudget) > 0)
            throw new ServiceException("实际成本合计不能超过项目分类预算");
        if (workPackageId != null) {
            BigDecimal workPackageBudget =
                    workPackageBudget(workPackageId, old.getCostCategoryId()).getBudgetAmount();
            if (newWorkPackageTotal.compareTo(workPackageBudget) > 0)
                throw new ServiceException("实际成本合计不能超过该工作包预算");
        }
        old.setWorkPackageId(workPackageId);
        old.setActualAmount(amount);
        old.setOccurDate(date);
        old.setDescription(description);
        old.setUpdateBy(operator);
        mapper.update(old);
        return mapper.selectById(old.getActualCostId());
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long costId, String operator, Long userId) {
        ProjectActualCost old = require(projectId, costId);
        if (!"MANUAL".equals(old.getSourceType()))
            throw new ServiceException("采购入库来源的实际成本请通过采购入库弃审冲销");
        assertEditable(projectId, userId);
        assertOwned(old, operator, userId);
        mapper.deleteById(costId);
    }

    @Override
    public ProjectBudgetExecutionSummary execution(Long projectId, Long userId) {
        assertViewable(projectId, userId);
        ProjectInfo project = requireProject(projectId);
        ProjectBudgetExecutionSummary result = new ProjectBudgetExecutionSummary();
        result.setProjectId(projectId);
        result.setBudgetRequired(project.getBudgetRequired());
        if (!"1".equals(project.getBudgetRequired())) {
            result.setRows(List.of());
            return result;
        }
        Map<Long, BigDecimal> categoryActual = index(mapper.categoryTotals(projectId));
        List<ProjectBudgetLine> lines = budgetMapper.selectByProjectId(projectId);
        Map<String, BigDecimal> allocated = new LinkedHashMap<>();
        for (ProjectWorkPackageBudgetLine line :
                workPackageBudgetMapper.selectByProjectId(projectId)) {
            allocated.merge(
                    String.valueOf(line.getCostCategoryId()),
                    line.getBudgetAmount(),
                    BigDecimal::add);
        }
        List<ProjectBudgetExecutionRow> rows = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        for (ProjectBudgetLine line : lines) {
            BudgetNumbers numbers = numbers(line, categoryActual, allocated);
            rows.add(numbers.row);
            totalBudget = totalBudget.add(numbers.row.getProjectBudget());
            totalActual = totalActual.add(numbers.row.getActualCostAmount());
        }
        result.setRows(rows);
        result.setTotalBudget(totalBudget);
        result.setTotalAllocated(
                rows.stream()
                        .map(ProjectBudgetExecutionRow::getAllocatedAmount)
                        .reduce(ZERO, BigDecimal::add));
        result.setTotalActualCost(totalActual);
        result.setTotalRemaining(totalBudget.subtract(totalActual));
        result.setExecutionRate(rate(totalActual, totalBudget));
        return result;
    }

    private BudgetNumbers numbers(
            ProjectBudgetLine line,
            Map<Long, BigDecimal> categoryActual,
            Map<String, BigDecimal> allocated) {
        BigDecimal budget = money(line.getBudgetAmount());
        BigDecimal actual = categoryActual.getOrDefault(line.getCostCategoryId(), ZERO);
        BigDecimal allocatedAmount =
                allocated.getOrDefault(String.valueOf(line.getCostCategoryId()), ZERO);
        ProjectBudgetExecutionRow row = new ProjectBudgetExecutionRow();
        row.setCostCategoryId(line.getCostCategoryId());
        row.setCategoryCode(line.getCategoryCode());
        row.setCategoryName(line.getCategoryName());
        row.setCategoryPath(line.getCategoryPath());
        row.setCategoryStatus(line.getCategoryStatus());
        row.setProjectBudget(budget);
        row.setAllocatedAmount(allocatedAmount);
        row.setRemainingAllocation(budget.subtract(allocatedAmount));
        row.setActualCostAmount(actual);
        row.setRemainingBudget(budget.subtract(actual));
        row.setExecutionRate(rate(actual, budget));
        return new BudgetNumbers(row);
    }

    private static final class BudgetNumbers {
        private final ProjectBudgetExecutionRow row;

        private BudgetNumbers(ProjectBudgetExecutionRow row) {
            this.row = row;
        }
    }

    private ProjectActualCost prepare(Long projectId, ProjectActualCost cost, String operator) {
        ProjectActualCost prepared = new ProjectActualCost();
        prepared.setProjectId(projectId);
        prepared.setCostCategoryId(requireId(cost.getCostCategoryId(), "成本类别"));
        CostCategory category =
                costCategoryService.options().stream()
                        .filter(
                                item ->
                                        item.getCostCategoryId()
                                                .equals(prepared.getCostCategoryId()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("实际成本只能选择有效末级成本类别"));
        prepared.setCategoryCode(category.getCategoryCode());
        prepared.setCategoryName(category.getCategoryName());
        prepared.setCategoryPath(category.getFullPath());
        prepared.setActualAmount(amount(cost.getActualAmount(), "实际成本金额"));
        prepared.setOccurDate(requireDate(cost.getOccurDate()));
        prepared.setDescription(requireText(cost.getDescription()));
        prepared.setWorkPackageId(cost.getWorkPackageId());
        prepared.setCreateBy(operator);
        return prepared;
    }

    private void validateWorkPackageChange(Long projectId, Long workPackageId) {
        if (workPackageId == null) return;
        ProjectWbsNode node = wbsMapper.selectById(workPackageId);
        if (node == null
                || !projectId.equals(node.getProjectId())
                || !"WORK_PACKAGE".equals(node.getNodeType()))
            throw new ServiceException("工作包不存在或不属于当前项目");
        if (WbsStatus.COMPLETED.matches(node.getStatus()))
            throw new ServiceException("已完成工作包不允许登记或调增实际成本");
    }

    private boolean sameWorkPackage(ProjectActualCost old, Long workPackageId) {
        return old.getWorkPackageId() != null && old.getWorkPackageId().equals(workPackageId);
    }

    private void requireCategoryAndBudget(Long projectId, Long costCategoryId) {
        if (costCategoryId == null) throw new ServiceException("成本类别不能为空");
        costCategoryService.options().stream()
                .filter(item -> item.getCostCategoryId().equals(costCategoryId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("该成本类别无效或不可用"));
        requireBudgetLine(projectId, costCategoryId);
    }

    private void assertEditable(Long projectId, Long userId) {
        assertViewable(projectId, userId);
        assertEditableStatus(projectId);
    }

    private void assertViewable(Long projectId, Long userId) {
        ProjectInfo project = requireProject(projectId);
        if (!teamService.isActiveMember(projectId, userId) && !isManager(project, userId))
            throw new ServiceException("您无权查看该项目实际成本");
    }

    private void assertEditableStatus(Long projectId) {
        ProjectInfo project = requireProject(projectId);
        if (!isEditableStatus(project)) throw new ServiceException("仅执行中或暂停中项目可登记实际成本");
    }

    private boolean isEditableStatus(ProjectInfo project) {
        return ProjectStatus.ACTIVE.matches(project.getStatus())
                || ProjectStatus.PAUSED.matches(project.getStatus());
    }

    private void assertOwned(ProjectActualCost cost, String operator, Long userId) {
        ProjectInfo project = requireProject(cost.getProjectId());
        if (isManager(project, userId) || operator.equals(cost.getCreateBy())) return;
        throw new ServiceException("仅登记人本人或项目负责人可更正或删除实际成本");
    }

    private boolean isManager(ProjectInfo project, Long userId) {
        return project.getManagerId() != null && project.getManagerId().equals(userId);
    }

    private ProjectInfo requireProject(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) throw new ServiceException("项目不存在");
        return project;
    }

    private ProjectActualCost require(Long projectId, Long costId) {
        ProjectActualCost cost = costId == null ? null : mapper.selectById(costId);
        if (cost == null || !projectId.equals(cost.getProjectId()))
            throw new ServiceException("实际成本记录不存在或不属于当前项目");
        return cost;
    }

    private ProjectBudgetLine requireBudgetLine(Long projectId, Long costCategoryId) {
        return budgetMapper.selectByProjectId(projectId).stream()
                .filter(line -> line.getCostCategoryId().equals(costCategoryId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("该项目成本类别未配置项目分类预算"));
    }

    private ProjectWorkPackageBudgetLine workPackageBudget(Long workPackageId, Long categoryId) {
        return workPackageBudgetMapper.selectByWorkPackageId(workPackageId).stream()
                .filter(line -> line.getCostCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("该工作包未分配该成本类别预算"));
    }

    private void requireWorkPackageBudget(
            Long projectId, Long workPackageId, ProjectActualCost cost) {
        ProjectWbsNode node = wbsMapper.selectById(workPackageId);
        if (node == null
                || !projectId.equals(node.getProjectId())
                || !"WORK_PACKAGE".equals(node.getNodeType()))
            throw new ServiceException("工作包不存在或不属于当前项目");
        if (WbsStatus.COMPLETED.matches(node.getStatus()))
            throw new ServiceException("已完成工作包不允许登记或调增实际成本");
        workPackageBudget(workPackageId, cost.getCostCategoryId());
    }

    private BigDecimal amount(BigDecimal value, String label) {
        try {
            BigDecimal amount = value == null ? null : value.setScale(2, RoundingMode.UNNECESSARY);
            if (amount == null || amount.signum() <= 0) throw new NumberFormatException();
            return amount;
        } catch (Exception e) {
            throw new ServiceException(label + "必须大于0且最多保留两位小数");
        }
    }

    private LocalDate requireDate(LocalDate date) {
        if (date == null) throw new ServiceException("发生日期不能为空");
        return date;
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) throw new ServiceException("成本事由不能为空");
        String text = value.trim();
        if (text.length() > 500) throw new ServiceException("成本事由不能超过500字");
        return text;
    }

    private Long requireId(Long value, String label) {
        if (value == null) throw new ServiceException(label + "不能为空");
        return value;
    }

    private Map<Long, BigDecimal> index(List<ProjectActualCostAggregate> items) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (ProjectActualCostAggregate item : items)
            result.put(item.getCostCategoryId(), money(item.getActualAmount()));
        return result;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal actual, BigDecimal budget) {
        if (budget == null || budget.signum() <= 0) return ZERO;
        return actual.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP);
    }

    private static final BigDecimal ZERO = BigDecimal.ZERO;
}
