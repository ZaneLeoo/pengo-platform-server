package com.ruoyi.projectmanagement.project.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.common.enums.InitiationApprovalStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectPhaseStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WorkItemType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import com.ruoyi.projectmanagement.phase.mapper.ProjectPhaseMapper;
import com.ruoyi.projectmanagement.phase.service.IProjectPhaseService;
import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 项目主档业务实现。
 */
@Service
public class ProjectInfoServiceImpl implements IProjectInfoService {

    private final ProjectInfoMapper projectMapper;
    private final ProjectCategoryMapper categoryMapper;
    private final ProjectPersonMapper personMapper;
    private final IProjectTeamService teamService;
    private final IProjectPhaseService phaseService;
    private final ProjectPhaseMapper phaseMapper;
    private final ObjectMapper objectMapper;
    private final ProjectWorkItemMapper workItemMapper;
    private final ProjectDeliverableMapper deliverableMapper;

    public ProjectInfoServiceImpl(ProjectInfoMapper projectMapper, ProjectCategoryMapper categoryMapper,
            ProjectPersonMapper personMapper, IProjectTeamService teamService, IProjectPhaseService phaseService,
            ProjectPhaseMapper phaseMapper, ObjectMapper objectMapper, ProjectWorkItemMapper workItemMapper,
            ProjectDeliverableMapper deliverableMapper) {
        this.projectMapper = projectMapper;
        this.categoryMapper = categoryMapper;
        this.personMapper = personMapper;
        this.teamService = teamService;
        this.phaseService = phaseService;
        this.phaseMapper = phaseMapper;
        this.objectMapper = objectMapper;
        this.workItemMapper = workItemMapper;
        this.deliverableMapper = deliverableMapper;
    }

    @Override
    public List<ProjectInfo> selectProjectInfoList(ProjectInfo project) {
        return projectMapper.selectProjectInfoList(project);
    }

    @Override
    public ProjectInfo selectProjectInfoById(Long id) {
        return projectMapper.selectProjectInfoById(id);
    }

    @Override
    public boolean checkProjectCodeUnique(ProjectInfo p) {
        Long id = StringUtils.isNull(p.getProjectId()) ? -1L : p.getProjectId();
        ProjectInfo e = projectMapper.selectProjectInfoByCode(p.getProjectCode());
        return e == null || e.getProjectId().longValue() == id.longValue();
    }

    @Override
    @Transactional
    public int insertProjectInfo(ProjectInfo p) {
        p.setStatus(ProjectStatus.DRAFT.getCode());
        p.setProgress(0);
        p.setApplicant(p.getCreateBy());
        if (p.getBudgetRequired() == null) {
            p.setBudgetRequired("0");
        }
        validate(p);
        int rows = projectMapper.insertProjectInfo(p);
        if (rows > 0) {
            teamService.ensureManager(p.getProjectId(), p.getManagerId(), null, p.getCreateBy());
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateProjectInfo(ProjectInfo p) {
        ProjectInfo existing = projectMapper.selectProjectInfoById(p.getProjectId());
        if (existing == null) {
            throw new ServiceException("项目不存在");
        }
        if (ProjectStatus.PENDING_APPROVAL.matches(existing.getStatus())) {
            throw new ServiceException("项目正在立项审批中，不能修改申请材料");
        }
        if (!ProjectStatus.DRAFT.matches(existing.getStatus()) && !ProjectStatus.APPROVED.matches(existing.getStatus())) {
            throw new ServiceException("当前项目状态不能修改基本信息");
        }
        p.setStatus(existing.getStatus());
        p.setActualStartDate(existing.getActualStartDate());
        p.setActualEndDate(existing.getActualEndDate());
        p.setPauseReason(existing.getPauseReason());
        validate(p);
        int rows = projectMapper.updateProjectInfo(p);
        if (rows > 0 && !existing.getManagerId().equals(p.getManagerId())) {
            teamService.ensureManager(p.getProjectId(), p.getManagerId(), existing.getManagerId(), p.getUpdateBy());
        }
        return rows;
    }

    @Override
    public int deleteProjectInfoByIds(Long[] ids) {
        return projectMapper.deleteProjectInfoByIds(ids);
    }

    @Override
    public int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        assertOperator(project.getManagerCode(), operator, "只有项目负责人或admin可以执行该项目动作");
        String from = project.getStatus();
        String action = request.getAction().trim().toUpperCase();
        String to;
        switch (action) {
            case "START" -> {
                if (!ProjectStatus.APPROVED.matches(from)) {
                    throw new ServiceException("只有已正式立项的项目可以启动");
                }
                @SuppressWarnings("unchecked")
                List<String> issues = (List<String>) startReadiness(projectId).get("issues");
                if (!issues.isEmpty()) {
                    throw new ServiceException("项目计划尚未准备完成：\n- " + String.join("\n- ", issues));
                }
                to = ProjectStatus.ACTIVE.getCode();
                project.setActualStartDate(LocalDate.now());
            }
            case "PAUSE" -> {
                if (!ProjectStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的项目可以暂停");
                }
                if (StringUtils.isBlank(request.getReason())) {
                    throw new ServiceException("暂停项目必须填写原因");
                }
                to = ProjectStatus.PAUSED.getCode();
                project.setPauseReason(request.getReason().trim());
            }
            case "RESUME" -> {
                if (!ProjectStatus.PAUSED.matches(from)) {
                    throw new ServiceException("只有已暂停项目可以恢复");
                }
                to = ProjectStatus.ACTIVE.getCode();
                project.setPauseReason(null);
            }
            case "COMPLETE" -> {
                if (!ProjectStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的项目可以完成");
                }
                if (!phaseService.allCompleted(projectId)) {
                    throw new ServiceException("请先完成项目的全部阶段");
                }
                to = ProjectStatus.COMPLETED.getCode();
                project.setActualEndDate(LocalDate.now());
                project.setProgress(100);
            }
            default -> throw new ServiceException("不支持的项目生命周期动作");
        }
        project.setStatus(to);
        project.setUpdateBy(operator);
        int rows = projectMapper.updateLifecycle(project);
        if (rows > 0) {
            projectMapper.insertLifecycleLog(projectId, action, from, to, request.getReason(), operator);
        }
        return rows;
    }

    @Override
    public Map<String, Object> startReadiness(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        List<String> issues = new ArrayList<>();
        List<ProjectPhase> phases = phaseMapper.selectList(new ProjectPhase() {{
            setProjectId(projectId);
        }});
        if (phases.isEmpty()) {
            issues.add("尚未建立正式项目阶段");
        }
        ProjectWorkItem filter = new ProjectWorkItem();
        filter.setProjectId(projectId);
        filter.setItemType(WorkItemType.TASK.getCode());
        List<ProjectWorkItem> tasks = workItemMapper.selectList(filter);
        Map<Long, ProjectWorkItem> byId = new HashMap<>();
        for (ProjectWorkItem t : tasks) {
            byId.put(t.getItemId(), t);
        }
        for (ProjectPhase phase : phases) {
            String p = "阶段【" + phase.getPhaseName() + "】";
            if (phase.getOwnerId() == null) {
                issues.add(p + "未设置负责人");
            } else if (!teamService.isActiveMember(projectId, phase.getOwnerId())) {
                issues.add(p + "负责人不属于当前在组成员");
            }
            if (phase.getStartDate() == null || phase.getEndDate() == null) {
                issues.add(p + "未完整设置计划开始/结束日期");
            } else if (phase.getStartDate().isBefore(project.getStartDate())
                    || phase.getEndDate().isAfter(project.getEndDate())) {
                issues.add(p + "计划日期超出项目计划周期");
            }
            if (workItemMapper.countLeafTasksByPhaseId(phase.getPhaseId()) == 0) {
                issues.add(p + "尚未配置末级WBS任务");
            }
        }
        for (ProjectWorkItem task : tasks) {
            String p = "WBS【" + task.getItemCode() + " · " + task.getItemName() + "】";
            if (task.getOwnerId() == null) {
                issues.add(p + "未设置负责人");
            } else if (!teamService.isActiveMember(projectId, task.getOwnerId())) {
                issues.add(p + "负责人不属于当前在组成员");
            }
            if (task.getStartDate() == null || task.getDueDate() == null) {
                issues.add(p + "未完整设置计划开始/结束日期");
            }
            ProjectPhase phase = task.getPhaseId() == null ? null : phaseMapper.selectById(task.getPhaseId());
            if (phase == null) {
                issues.add(p + "未关联有效阶段");
            } else if (task.getStartDate() != null && task.getDueDate() != null && phase.getStartDate() != null
                    && phase.getEndDate() != null
                    && (task.getStartDate().isBefore(phase.getStartDate())
                            || task.getDueDate().isAfter(phase.getEndDate()))) {
                issues.add(p + "计划日期超出所属阶段");
            }
            if (task.getParentId() != null && task.getParentId() != 0) {
                ProjectWorkItem parent = byId.get(task.getParentId());
                if (parent == null) {
                    issues.add(p + "上级任务不存在");
                } else if (task.getStartDate() != null && task.getDueDate() != null && parent.getStartDate() != null
                        && parent.getDueDate() != null
                        && (task.getStartDate().isBefore(parent.getStartDate())
                                || task.getDueDate().isAfter(parent.getDueDate()))) {
                    issues.add(p + "计划日期超出上级任务");
                }
            }
            if ("1".equals(task.getDeliverableRequired())
                    && deliverableMapper.countByTaskId(task.getItemId()) == 0) {
                issues.add(p + "要求交付物，但尚未配置交付项");
            }
        }
        return Map.of("passed", issues.isEmpty(), "issues", issues);
    }

    @Override
    public List<ProjectPreliminaryPlan> preliminaryPlans(Long projectId) {
        return projectMapper.selectPreliminaryPlans(projectId);
    }

    @Override
    public int addPreliminaryPlan(ProjectPreliminaryPlan p, String op) {
        ProjectInfo project = editable(p.getProjectId(), op);
        validatePlan(p, project);
        p.setCreateBy(op);
        if (p.getSortOrder() == null) {
            p.setSortOrder(0);
        }
        return projectMapper.insertPreliminaryPlan(p);
    }

    @Override
    public int updatePreliminaryPlan(ProjectPreliminaryPlan p, String op) {
        ProjectPreliminaryPlan old = projectMapper.selectPreliminaryPlan(p.getPlanId());
        if (old == null) {
            throw new ServiceException("初步计划不存在");
        }
        if (old.getConvertedPhaseId() != null) {
            throw new ServiceException("该初步阶段已转为正式阶段，不能修改");
        }
        ProjectInfo project = editable(old.getProjectId(), op);
        p.setProjectId(old.getProjectId());
        validatePlan(p, project);
        p.setUpdateBy(op);
        return projectMapper.updatePreliminaryPlan(p);
    }

    @Override
    public int deletePreliminaryPlan(Long id, String op) {
        ProjectPreliminaryPlan p = projectMapper.selectPreliminaryPlan(id);
        if (p == null) {
            throw new ServiceException("初步计划不存在");
        }
        if (p.getConvertedPhaseId() != null) {
            throw new ServiceException("该初步阶段已转为正式阶段，不能删除");
        }
        editable(p.getProjectId(), op);
        return projectMapper.deletePreliminaryPlan(id);
    }

    @Override
    @Transactional
    public int submitInitiation(Long id, String op) {
        ProjectInfo p = editable(id, op);
        List<ProjectPreliminaryPlan> plans = projectMapper.selectPreliminaryPlans(id);
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(p.getProjectBackground())) {
            missing.add("项目背景");
        }
        if (StringUtils.isBlank(p.getProjectScope())) {
            missing.add("项目范围");
        }
        if (StringUtils.isBlank(p.getExpectedOutcome())) {
            missing.add("预期成果");
        }
        if (StringUtils.isBlank(p.getResourceRequirement())) {
            missing.add("资源需求");
        }
        if (StringUtils.isBlank(p.getMajorRisk())) {
            missing.add("主要风险");
        }
        if (StringUtils.isBlank(p.getFeasibilityConclusion())) {
            missing.add("可行性综合结论");
        }
        if (plans.isEmpty()) {
            missing.add("初步计划");
        }
        if (!missing.isEmpty()) {
            throw new ServiceException("请先完善立项材料：" + String.join("、", missing));
        }
        if ("1".equals(p.getBudgetRequired()) && p.getBudgetAmount() == null) {
            throw new ServiceException("项目需要预算，请填写预算总额");
        }
        int version = (p.getInitiationVersion() == null ? 0 : p.getInitiationVersion()) + 1;
        ProjectInitiationApproval a = new ProjectInitiationApproval();
        a.setProjectId(id);
        a.setVersionNo(version);
        a.setSubmitBy(op);
        a.setStatus(InitiationApprovalStatus.PENDING.getCode());
        try {
            a.setSnapshotJson(objectMapper
                    .writeValueAsString(Map.of("project", p, "preliminaryPlans", plans)));
        } catch (Exception e) {
            throw new ServiceException("生成立项申请快照失败");
        }
        projectMapper.insertApproval(a);
        p.setStatus(ProjectStatus.PENDING_APPROVAL.getCode());
        p.setInitiationVersion(version);
        p.setUpdateBy(op);
        return projectMapper.updateInitiationState(p);
    }

    @Override
    @Transactional
    public int reviewInitiation(Long id, InitiationReviewRequest r, String op) {
        if (!"admin".equalsIgnoreCase(op)) {
            throw new ServiceException("只有admin可以审批立项申请");
        }
        ProjectInfo p = projectMapper.selectProjectInfoById(id);
        if (p == null || !ProjectStatus.PENDING_APPROVAL.matches(p.getStatus())) {
            throw new ServiceException("项目不在立项审批中");
        }
        ProjectInitiationApproval a = projectMapper.selectPendingApproval(id);
        if (a == null) {
            throw new ServiceException("待审批记录不存在");
        }
        String result = r.getResult().trim().toUpperCase();
        boolean approved = InitiationApprovalStatus.APPROVED.matches(result);
        if (!approved && !InitiationApprovalStatus.RETURNED.matches(result)) {
            throw new ServiceException("审批结果不正确");
        }
        if (!approved && StringUtils.isBlank(r.getComment())) {
            throw new ServiceException("退回必须填写审批意见");
        }
        a.setStatus(result);
        a.setReviewBy(op);
        a.setReviewComment(r.getComment());
        int rows = projectMapper.reviewApproval(a);
        p.setStatus(approved ? ProjectStatus.APPROVED.getCode() : ProjectStatus.DRAFT.getCode());
        p.setInitiationTime(approved ? LocalDateTime.now() : null);
        p.setUpdateBy(op);
        projectMapper.updateInitiationState(p);
        if (approved) {
            convertPlans(p, op);
        }
        return rows;
    }

    @Override
    public List<ProjectInitiationApproval> approvalHistory(Long id) {
        return projectMapper.selectApprovals(id);
    }

    @Override
    public ProjectInitiationApproval approvalSnapshot(Long projectId, Long approvalId) {
        return projectMapper.selectApprovals(projectId).stream()
                .filter(x -> x.getApprovalId().equals(approvalId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("审批记录不存在"));
    }

    private ProjectInfo editable(Long id, String op) {
        ProjectInfo p = projectMapper.selectProjectInfoById(id);
        if (p == null) {
            throw new ServiceException("项目不存在");
        }
        assertOperator(p.getManagerCode(), op, "只有项目负责人或admin可以维护立项申请");
        if (!ProjectStatus.DRAFT.matches(p.getStatus())) {
            throw new ServiceException("只有申请草稿可以修改或提交");
        }
        return p;
    }

    private void validatePlan(ProjectPreliminaryPlan plan, ProjectInfo project) {
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new ServiceException("初步阶段结束日期不能早于开始日期");
        }
        if (plan.getStartDate().isBefore(project.getStartDate())
                || plan.getEndDate().isAfter(project.getEndDate())) {
            throw new ServiceException("初步阶段日期必须在项目预计日期范围内");
        }
    }

    private void convertPlans(ProjectInfo p, String op) {
        for (ProjectPreliminaryPlan plan : projectMapper.selectPreliminaryPlans(p.getProjectId())) {
            if (plan.getConvertedPhaseId() != null) {
                continue;
            }
            ProjectPhase phase = new ProjectPhase();
            phase.setProjectId(p.getProjectId());
            phase.setPhaseCode("INIT-" + plan.getPlanId());
            phase.setPhaseName(plan.getPhaseName());
            phase.setStartDate(plan.getStartDate());
            phase.setEndDate(plan.getEndDate());
            phase.setStatus(ProjectPhaseStatus.NOT_STARTED.getCode());
            phase.setSortOrder(plan.getSortOrder());
            phase.setRemark("关键里程碑：" + plan.getMilestoneName()
                    + (StringUtils.isBlank(plan.getPhaseGoal()) ? "" : "；" + plan.getPhaseGoal()));
            phase.setCreateBy(op);
            phaseMapper.insert(phase);
            projectMapper.markPlanConverted(plan.getPlanId(), phase.getPhaseId());
        }
    }

    private void validate(ProjectInfo p) {
        if (p.getEndDate().isBefore(p.getStartDate())) {
            throw new ServiceException("计划结束日期不能早于开始日期");
        }
        if (categoryMapper.selectProjectCategoryById(p.getCategoryId()) == null) {
            throw new ServiceException("项目分类不存在");
        }
        ProjectPerson manager = personMapper.selectProjectPersonById(p.getManagerId());
        if (manager == null || !"0".equals(manager.getStatus())) {
            throw new ServiceException("请选择启用状态的项目负责人");
        }
        if (p.getProgress() == null) {
            p.setProgress(0);
        }
        if (StringUtils.isBlank(p.getStatus())) {
            p.setStatus(ProjectStatus.DRAFT.getCode());
        }
    }

    private void assertOperator(String ownerCode, String operator, String message) {
        if (!"admin".equalsIgnoreCase(operator)
                && (StringUtils.isBlank(ownerCode) || !ownerCode.equalsIgnoreCase(operator))) {
            throw new ServiceException(message);
        }
    }
}
