package com.ruoyi.projectmanagement.project.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.common.enums.InitiationApprovalStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.TaskType;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** 项目主档、立项和项目生命周期业务。 */
@Service
public class ProjectInfoServiceImpl implements IProjectInfoService {
    private final ProjectInfoMapper projectMapper;
    private final ProjectCategoryMapper categoryMapper;
    private final ProjectPersonMapper personMapper;
    private final IProjectTeamService teamService;
    private final IProjectWbsService wbsService;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectTaskMapper taskMapper;
    private final ProjectDeliverableMapper deliverableMapper;
    private final ObjectMapper objectMapper;

    public ProjectInfoServiceImpl(ProjectInfoMapper projectMapper, ProjectCategoryMapper categoryMapper,
            ProjectPersonMapper personMapper, IProjectTeamService teamService, IProjectWbsService wbsService,
            ProjectWbsMapper wbsMapper, ProjectTaskMapper taskMapper,
            ProjectDeliverableMapper deliverableMapper, ObjectMapper objectMapper) {
        this.projectMapper = projectMapper;
        this.categoryMapper = categoryMapper;
        this.personMapper = personMapper;
        this.teamService = teamService;
        this.wbsService = wbsService;
        this.wbsMapper = wbsMapper;
        this.taskMapper = taskMapper;
        this.deliverableMapper = deliverableMapper;
        this.objectMapper = objectMapper;
    }

    @Override public List<ProjectInfo> selectProjectInfoList(ProjectInfo p) { return projectMapper.selectProjectInfoList(p); }
    @Override public ProjectInfo selectProjectInfoById(Long id) { return projectMapper.selectProjectInfoById(id); }
    @Override public boolean checkProjectCodeUnique(ProjectInfo p) {
        Long id = p.getProjectId() == null ? -1L : p.getProjectId();
        ProjectInfo found = projectMapper.selectProjectInfoByCode(p.getProjectCode());
        return found == null || found.getProjectId().equals(id);
    }

    @Override @Transactional
    public int insertProjectInfo(ProjectInfo p) {
        p.setStatus(ProjectStatus.DRAFT.getCode()); p.setProgress(0); p.setApplicant(p.getCreateBy());
        if (p.getBudgetRequired() == null) p.setBudgetRequired("0");
        validate(p);
        int rows = projectMapper.insertProjectInfo(p);
        if (rows > 0) teamService.ensureManager(p.getProjectId(), p.getManagerId(), null, p.getCreateBy());
        return rows;
    }

    @Override @Transactional
    public int updateProjectInfo(ProjectInfo p) {
        ProjectInfo old = requiredProject(p.getProjectId());
        if (ProjectStatus.PENDING_APPROVAL.matches(old.getStatus())) throw new ServiceException("项目正在立项审批中，不能修改申请材料");
        if (!ProjectStatus.DRAFT.matches(old.getStatus()) && !ProjectStatus.APPROVED.matches(old.getStatus())) throw new ServiceException("当前项目状态不能修改基本信息");
        p.setStatus(old.getStatus()); p.setActualStartDate(old.getActualStartDate()); p.setActualEndDate(old.getActualEndDate()); p.setPauseReason(old.getPauseReason());
        validate(p);
        int rows = projectMapper.updateProjectInfo(p);
        if (rows > 0 && !old.getManagerId().equals(p.getManagerId())) teamService.ensureManager(p.getProjectId(), p.getManagerId(), old.getManagerId(), p.getUpdateBy());
        return rows;
    }

    @Override public int deleteProjectInfoByIds(Long[] ids) { return projectMapper.deleteProjectInfoByIds(ids); }

    @Override @Transactional
    public int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator) {
        ProjectInfo p = requiredProject(projectId);
        assertOperator(p.getManagerCode(), operator, "只有项目负责人或admin可以执行该项目动作");
        String from = p.getStatus(); String action = request.getAction().trim().toUpperCase(); String to;
        switch (action) {
            case "START" -> {
                if (!ProjectStatus.APPROVED.matches(from)) throw new ServiceException("只有已正式立项的项目可以启动");
                @SuppressWarnings("unchecked") List<String> issues = (List<String>) startReadiness(projectId).get("issues");
                if (!issues.isEmpty()) throw new ServiceException("项目计划尚未准备完成：\n- " + String.join("\n- ", issues));
                to = ProjectStatus.ACTIVE.getCode(); p.setActualStartDate(LocalDate.now());
            }
            case "PAUSE" -> {
                if (!ProjectStatus.ACTIVE.matches(from)) throw new ServiceException("只有执行中的项目可以暂停");
                if (StringUtils.isBlank(request.getReason())) throw new ServiceException("暂停项目必须填写原因");
                to = ProjectStatus.PAUSED.getCode(); p.setPauseReason(request.getReason().trim());
            }
            case "RESUME" -> {
                if (!ProjectStatus.PAUSED.matches(from)) throw new ServiceException("只有已暂停项目可以恢复");
                to = ProjectStatus.ACTIVE.getCode(); p.setPauseReason(null);
            }
            case "COMPLETE" -> {
                if (!ProjectStatus.ACTIVE.matches(from)) throw new ServiceException("只有执行中的项目可以完成");
                if (!wbsService.allWorkPackagesCompleted(projectId)) throw new ServiceException("请先完成项目的全部工作包");
                to = ProjectStatus.COMPLETED.getCode(); p.setActualEndDate(LocalDate.now()); p.setProgress(100);
            }
            default -> throw new ServiceException("不支持的项目生命周期动作");
        }
        p.setStatus(to); p.setUpdateBy(operator);
        int rows = projectMapper.updateLifecycle(p);
        if (rows > 0) projectMapper.insertLifecycleLog(projectId, action, from, to, request.getReason(), operator);
        return rows;
    }

    @Override
    public Map<String, Object> startReadiness(Long projectId) {
        ProjectInfo project = requiredProject(projectId);
        List<String> issues = new ArrayList<>();
        ProjectWbsNode wf = new ProjectWbsNode(); wf.setProjectId(projectId);
        List<ProjectWbsNode> nodes = wbsMapper.selectList(wf);
        Map<Long, ProjectWbsNode> wbsById = new HashMap<>();
        nodes.forEach(x -> wbsById.put(x.getWbsId(), x));
        List<ProjectWbsNode> roots = nodes.stream().filter(x -> x.getParentId() == null || x.getParentId() == 0).toList();
        if (roots.isEmpty()) issues.add("至少需要一个顶层WBS");
        for (ProjectWbsNode node : nodes) {
            String label = (WbsNodeType.WORK_PACKAGE.matches(node.getNodeType()) ? "工作包" : "WBS") + "【" + node.getWbsCode() + " · " + node.getWbsName() + "】";
            if (WbsNodeType.SUMMARY.matches(node.getNodeType())) {
                if (node.getChildCount() == null || node.getChildCount() == 0) issues.add(label + "为空，WBS分支必须最终落到工作包");
                continue;
            }
            validateWorkPackageReadiness(project, node, label, issues);
        }
        for (ProjectWbsNode root : roots) {
            String label = "顶层WBS【" + root.getWbsCode() + " · " + root.getWbsName() + "】";
            if (root.getPlanStartDate() != null && root.getTargetStartDate() != null && root.getPlanStartDate().isBefore(root.getTargetStartDate())) issues.add(label + "汇总开始日期早于立项批准目标窗口");
            if (root.getPlanEndDate() != null && root.getTargetEndDate() != null && root.getPlanEndDate().isAfter(root.getTargetEndDate())) issues.add(label + "汇总结束日期晚于立项批准目标窗口");
        }
        return Map.of("passed", issues.isEmpty(), "issues", issues);
    }

    private void validateWorkPackageReadiness(ProjectInfo project, ProjectWbsNode wp, String label, List<String> issues) {
        if (wp.getOwnerId() == null) issues.add(label + "未设置负责人");
        else if (!teamService.isActiveMember(project.getProjectId(), wp.getOwnerId())) issues.add(label + "负责人不属于当前在组成员");
        if (StringUtils.isBlank(wp.getAcceptanceCriteria())) issues.add(label + "未填写验收标准");
        if (StringUtils.isBlank(wp.getDefinitionOfDone())) issues.add(label + "未填写完成定义");
        if (wp.getPlanStartDate() == null || wp.getPlanEndDate() == null) issues.add(label + "未完整设置计划日期");
        else {
            if (wp.getPlanEndDate().isBefore(wp.getPlanStartDate())) issues.add(label + "计划结束日期早于开始日期");
            if (wp.getPlanStartDate().isBefore(project.getStartDate()) || wp.getPlanEndDate().isAfter(project.getEndDate())) issues.add(label + "计划日期超出项目周期");
        }
        if (deliverableMapper.countRequiredByWorkPackageId(wp.getWbsId()) == 0) issues.add(label + "至少需要一个必交正式交付物");
        ProjectTask filter = new ProjectTask(); filter.setWorkPackageId(wp.getWbsId());
        List<ProjectTask> tasks = taskMapper.selectList(filter);
        Map<Long, ProjectTask> taskById = new HashMap<>(); tasks.forEach(x -> taskById.put(x.getTaskId(), x));
        if (tasks.stream().noneMatch(x -> TaskType.EXECUTION.matches(x.getTaskType()))) issues.add(label + "至少需要一个末级执行任务");
        for (ProjectTask task : tasks) {
            String taskLabel = "任务【" + task.getTaskCode() + " · " + task.getTaskName() + "】";
            if (TaskType.SUMMARY.matches(task.getTaskType())) {
                if (task.getChildCount() == null || task.getChildCount() == 0) issues.add(taskLabel + "为汇总任务但没有下级任务");
                continue;
            }
            if (task.getAssigneeId() == null) issues.add(taskLabel + "未设置执行人");
            else if (!teamService.isActiveMember(project.getProjectId(), task.getAssigneeId())) issues.add(taskLabel + "执行人不属于当前在组成员");
            if (task.getPlanStartDate() == null || task.getPlanEndDate() == null) { issues.add(taskLabel + "未完整设置计划日期"); continue; }
            if (task.getPlanEndDate().isBefore(task.getPlanStartDate())) issues.add(taskLabel + "计划结束日期早于开始日期");
            if (wp.getPlanStartDate() != null && wp.getPlanEndDate() != null && (task.getPlanStartDate().isBefore(wp.getPlanStartDate()) || task.getPlanEndDate().isAfter(wp.getPlanEndDate()))) issues.add(taskLabel + "计划日期超出所属工作包");
            if (task.getParentTaskId() != null && task.getParentTaskId() != 0) {
                ProjectTask parent = taskById.get(task.getParentTaskId());
                if (parent == null) issues.add(taskLabel + "上级任务不存在");
                else if (parent.getPlanStartDate() != null && parent.getPlanEndDate() != null && (task.getPlanStartDate().isBefore(parent.getPlanStartDate()) || task.getPlanEndDate().isAfter(parent.getPlanEndDate()))) issues.add(taskLabel + "计划日期超出上级任务");
            }
        }
    }

    @Override public List<ProjectPreliminaryPlan> preliminaryPlans(Long projectId) { return projectMapper.selectPreliminaryPlans(projectId); }
    @Override public int addPreliminaryPlan(ProjectPreliminaryPlan p, String op) { ProjectInfo project = editable(p.getProjectId(), op); validatePlan(p, project); p.setCreateBy(op); if (p.getSortOrder() == null) p.setSortOrder(0); return projectMapper.insertPreliminaryPlan(p); }
    @Override public int updatePreliminaryPlan(ProjectPreliminaryPlan p, String op) {
        ProjectPreliminaryPlan old = projectMapper.selectPreliminaryPlan(p.getPlanId());
        if (old == null) throw new ServiceException("WBS概要不存在");
        if (old.getConvertedWbsId() != null) throw new ServiceException("该WBS概要已转为正式WBS，不能修改");
        ProjectInfo project = editable(old.getProjectId(), op); p.setProjectId(old.getProjectId()); validatePlan(p, project); p.setUpdateBy(op); return projectMapper.updatePreliminaryPlan(p);
    }
    @Override public int deletePreliminaryPlan(Long id, String op) {
        ProjectPreliminaryPlan p = projectMapper.selectPreliminaryPlan(id);
        if (p == null) throw new ServiceException("WBS概要不存在");
        if (p.getConvertedWbsId() != null) throw new ServiceException("该WBS概要已转为正式WBS，不能删除");
        editable(p.getProjectId(), op); return projectMapper.deletePreliminaryPlan(id);
    }

    @Override @Transactional
    public int submitInitiation(Long id, String op) {
        ProjectInfo p = editable(id, op); List<ProjectPreliminaryPlan> plans = projectMapper.selectPreliminaryPlans(id); List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(p.getProjectBackground())) missing.add("项目背景");
        if (StringUtils.isBlank(p.getProjectScope())) missing.add("项目范围");
        if (StringUtils.isBlank(p.getExpectedOutcome())) missing.add("预期成果");
        if (StringUtils.isBlank(p.getResourceRequirement())) missing.add("资源需求");
        if (StringUtils.isBlank(p.getMajorRisk())) missing.add("主要风险");
        if (StringUtils.isBlank(p.getFeasibilityConclusion())) missing.add("可行性综合结论");
        if (plans.isEmpty()) missing.add("WBS概要");
        if (!missing.isEmpty()) throw new ServiceException("请先完善立项材料：" + String.join("、", missing));
        if ("1".equals(p.getBudgetRequired()) && p.getBudgetAmount() == null) throw new ServiceException("项目需要预算，请填写预算总额");
        int version = (p.getInitiationVersion() == null ? 0 : p.getInitiationVersion()) + 1;
        ProjectInitiationApproval a = new ProjectInitiationApproval(); a.setProjectId(id); a.setVersionNo(version); a.setSubmitBy(op); a.setStatus(InitiationApprovalStatus.PENDING.getCode());
        try { a.setSnapshotJson(objectMapper.writeValueAsString(Map.of("project", p, "wbsOutlines", plans))); }
        catch (Exception e) { throw new ServiceException("生成立项申请快照失败"); }
        projectMapper.insertApproval(a); p.setStatus(ProjectStatus.PENDING_APPROVAL.getCode()); p.setInitiationVersion(version); p.setUpdateBy(op); return projectMapper.updateInitiationState(p);
    }

    @Override @Transactional
    public int reviewInitiation(Long id, InitiationReviewRequest r, String op) {
        if (!"admin".equalsIgnoreCase(op)) throw new ServiceException("只有admin可以审批立项申请");
        ProjectInfo p = requiredProject(id);
        if (!ProjectStatus.PENDING_APPROVAL.matches(p.getStatus())) throw new ServiceException("项目不在立项审批中");
        ProjectInitiationApproval a = projectMapper.selectPendingApproval(id); if (a == null) throw new ServiceException("待审批记录不存在");
        String result = r.getResult().trim().toUpperCase(); boolean approved = InitiationApprovalStatus.APPROVED.matches(result);
        if (!approved && !InitiationApprovalStatus.RETURNED.matches(result)) throw new ServiceException("审批结果不正确");
        if (!approved && StringUtils.isBlank(r.getComment())) throw new ServiceException("退回必须填写审批意见");
        a.setStatus(result); a.setReviewBy(op); a.setReviewComment(r.getComment()); int rows = projectMapper.reviewApproval(a);
        p.setStatus(approved ? ProjectStatus.APPROVED.getCode() : ProjectStatus.DRAFT.getCode()); p.setInitiationTime(approved ? LocalDateTime.now() : null); p.setUpdateBy(op); projectMapper.updateInitiationState(p);
        if (approved) convertPlans(p, op); return rows;
    }

    @Override public List<ProjectInitiationApproval> approvalHistory(Long id) { return projectMapper.selectApprovals(id); }
    @Override public ProjectInitiationApproval approvalSnapshot(Long projectId, Long approvalId) { return projectMapper.selectApprovals(projectId).stream().filter(x -> x.getApprovalId().equals(approvalId)).findFirst().orElseThrow(() -> new ServiceException("审批记录不存在")); }

    private void convertPlans(ProjectInfo p, String op) {
        int code = 1;
        for (ProjectPreliminaryPlan plan : projectMapper.selectPreliminaryPlans(p.getProjectId())) {
            if (plan.getConvertedWbsId() != null) { code++; continue; }
            ProjectWbsNode wbs = new ProjectWbsNode(); wbs.setProjectId(p.getProjectId()); wbs.setParentId(0L); wbs.setWbsCode(String.valueOf(code++));
            wbs.setNodeType(WbsNodeType.SUMMARY.name()); wbs.setWbsName(plan.getOutlineName()); wbs.setScopeDescription(plan.getOutlineDescription());
            wbs.setTargetStartDate(plan.getStartDate()); wbs.setTargetEndDate(plan.getEndDate()); wbs.setTargetMilestone(plan.getMilestoneName());
            wbs.setStatus(WbsStatus.NOT_STARTED.name()); wbs.setProgress(0); wbs.setSortOrder(plan.getSortOrder()); wbs.setCreateBy(op);
            wbsMapper.insert(wbs); projectMapper.markPlanConverted(plan.getPlanId(), wbs.getWbsId());
        }
    }

    private ProjectInfo editable(Long id, String op) { ProjectInfo p = requiredProject(id); assertOperator(p.getManagerCode(), op, "只有项目负责人或admin可以维护立项申请"); if (!ProjectStatus.DRAFT.matches(p.getStatus())) throw new ServiceException("只有申请草稿可以修改或提交"); return p; }
    private void validatePlan(ProjectPreliminaryPlan plan, ProjectInfo project) { if (plan.getEndDate().isBefore(plan.getStartDate())) throw new ServiceException("WBS概要结束日期不能早于开始日期"); if (plan.getStartDate().isBefore(project.getStartDate()) || plan.getEndDate().isAfter(project.getEndDate())) throw new ServiceException("WBS概要目标日期必须在项目预计日期范围内"); }
    private ProjectInfo requiredProject(Long id) { ProjectInfo p = projectMapper.selectProjectInfoById(id); if (p == null) throw new ServiceException("项目不存在"); return p; }
    private void validate(ProjectInfo p) { if (p.getEndDate().isBefore(p.getStartDate())) throw new ServiceException("计划结束日期不能早于开始日期"); if (categoryMapper.selectProjectCategoryById(p.getCategoryId()) == null) throw new ServiceException("项目分类不存在"); ProjectPerson manager = personMapper.selectProjectPersonById(p.getManagerId()); if (manager == null || !"0".equals(manager.getStatus())) throw new ServiceException("请选择启用状态的项目负责人"); if (p.getProgress() == null) p.setProgress(0); if (StringUtils.isBlank(p.getStatus())) p.setStatus(ProjectStatus.DRAFT.getCode()); }
    private void assertOperator(String ownerCode, String operator, String message) { if (!"admin".equalsIgnoreCase(operator) && (StringUtils.isBlank(ownerCode) || !ownerCode.equalsIgnoreCase(operator))) throw new ServiceException(message); }
}
