package com.ruoyi.projectmanagement.project.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.common.enums.InitiationApprovalStatus;
import com.ruoyi.projectmanagement.common.enums.LifecycleAction;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.TaskType;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.execution.domain.StartReadinessResult;
import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationAttachment;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.project.mapper.ProjectInitiationAttachmentMapper;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.common.enums.ProjectMemberStatus;
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

/**
 * 项目主档、立项和项目生命周期业务。
 */
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
    private final ProjectInitiationAttachmentMapper attachmentMapper;
    private final ObjectMapper objectMapper;

    public ProjectInfoServiceImpl(ProjectInfoMapper projectMapper, ProjectCategoryMapper categoryMapper,
            ProjectPersonMapper personMapper, IProjectTeamService teamService, IProjectWbsService wbsService,
            ProjectWbsMapper wbsMapper, ProjectTaskMapper taskMapper,
            ProjectDeliverableMapper deliverableMapper, ProjectInitiationAttachmentMapper attachmentMapper,
            ObjectMapper objectMapper) {
        this.projectMapper = projectMapper;
        this.categoryMapper = categoryMapper;
        this.personMapper = personMapper;
        this.teamService = teamService;
        this.wbsService = wbsService;
        this.wbsMapper = wbsMapper;
        this.taskMapper = taskMapper;
        this.deliverableMapper = deliverableMapper;
        this.attachmentMapper = attachmentMapper;
        this.objectMapper = objectMapper;
    }

    /** 查询项目列表。 */
    @Override
    public List<ProjectInfo> selectProjectInfoList(ProjectInfo project) {
        return projectMapper.selectProjectInfoList(project);
    }

    /** 查询项目详细。 */
    @Override
    public ProjectInfo selectProjectInfoById(Long id) {
        return projectMapper.selectProjectInfoById(id);
    }

    /** 校验项目编码是否唯一。 */
    @Override
    public boolean checkProjectCodeUnique(ProjectInfo project) {
        Long id = project.getProjectId() == null ? -1L : project.getProjectId();
        ProjectInfo found = projectMapper.selectProjectInfoByCode(project.getProjectCode());
        return found == null || found.getProjectId().equals(id);
    }

    /** 新增项目申请，自动创建负责人团队角色。 */
    @Override
    @Transactional
    public int insertProjectInfo(ProjectInfo project) {
        project.setStatus(ProjectStatus.DRAFT.getCode());
        project.setProgress(0);
        project.setApplicant(project.getCreateBy());
        if (project.getBudgetRequired() == null) {
            project.setBudgetRequired("0");
        }
        validate(project);
        int rows = projectMapper.insertProjectInfo(project);
        if (rows > 0) {
            teamService.ensureManager(project.getProjectId(), project.getManagerId(), null, project.getCreateBy());
        }
        return rows;
    }

    /** 修改项目基本信息，审批中与已执行项目不允许修改。 */
    @Override
    @Transactional
    public int updateProjectInfo(ProjectInfo project) {
        ProjectInfo old = requiredProject(project.getProjectId());
        if (ProjectStatus.PENDING_APPROVAL.matches(old.getStatus())) {
            throw new ServiceException("项目正在立项审批中，不能修改申请材料");
        }
        if (!ProjectStatus.DRAFT.matches(old.getStatus()) && !ProjectStatus.APPROVED.matches(old.getStatus())) {
            throw new ServiceException("当前项目状态不能修改基本信息");
        }
        project.setStatus(old.getStatus());
        project.setActualStartDate(old.getActualStartDate());
        project.setActualEndDate(old.getActualEndDate());
        project.setPauseReason(old.getPauseReason());
        validate(project);
        int rows = projectMapper.updateProjectInfo(project);
        if (rows > 0 && !old.getManagerId().equals(project.getManagerId())) {
            teamService.ensureManager(project.getProjectId(), project.getManagerId(), old.getManagerId(),
                    project.getUpdateBy());
        }
        return rows;
    }

    /** 批量删除项目。 */
    @Override
    public int deleteProjectInfoByIds(Long[] ids) {
        return projectMapper.deleteProjectInfoByIds(ids);
    }

    /** 执行项目生命周期动作（启动/暂停/恢复/完成）。 */
    @Override
    @Transactional
    public int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator) {
        ProjectInfo project = requiredProject(projectId);
        LifecycleAction action = LifecycleAction.fromCode(request.getAction());
        if (action == null) {
            throw new ServiceException("不支持的项目生命周期动作");
        }
        String from = project.getStatus();
        String to;
        switch (action) {
            case START -> {
                if (!ProjectStatus.APPROVED.matches(from)) {
                    throw new ServiceException("只有已正式立项的项目可以启动");
                }
                List<String> issues = startReadiness(projectId).getIssues();
                if (!issues.isEmpty()) {
                    throw new ServiceException("项目计划尚未准备完成：\n- " + String.join("\n- ", issues));
                }
                to = ProjectStatus.ACTIVE.getCode();
                project.setActualStartDate(LocalDate.now());
            }
            case PAUSE -> {
                if (!ProjectStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的项目可以暂停");
                }
                if (StringUtils.isBlank(request.getReason())) {
                    throw new ServiceException("暂停项目必须填写原因");
                }
                to = ProjectStatus.PAUSED.getCode();
                project.setPauseReason(request.getReason().trim());
            }
            case RESUME -> {
                if (!ProjectStatus.PAUSED.matches(from)) {
                    throw new ServiceException("只有已暂停项目可以恢复");
                }
                to = ProjectStatus.ACTIVE.getCode();
                project.setPauseReason(null);
            }
            case COMPLETE -> {
                if (!ProjectStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的项目可以完成");
                }
                if (!wbsService.allWorkPackagesCompleted(projectId)) {
                    throw new ServiceException("请先完成项目的全部工作包");
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
            projectMapper.insertLifecycleLog(projectId, action.getCode(), from, to, request.getReason(), operator);
        }
        return rows;
    }

    /** 项目启动前就绪检查：WBS结构、工作包与任务完整性。 */
    @Override
    public StartReadinessResult startReadiness(Long projectId) {
        ProjectInfo project = requiredProject(projectId);
        List<String> issues = new ArrayList<>();
        ProjectWbsNode filter = new ProjectWbsNode();
        filter.setProjectId(projectId);
        List<ProjectWbsNode> nodes = wbsMapper.selectList(filter);
        Map<Long, ProjectWbsNode> wbsById = new HashMap<>();
        nodes.forEach(x -> wbsById.put(x.getWbsId(), x));
        List<ProjectWbsNode> roots = nodes.stream()
                .filter(x -> x.getParentId() == null || x.getParentId() == 0)
                .toList();
        if (roots.isEmpty()) {
            issues.add("至少需要一个顶层WBS");
        }
        for (ProjectWbsNode node : nodes) {
            String label = (WbsNodeType.WORK_PACKAGE.matches(node.getNodeType()) ? "工作包" : "WBS")
                    + "【" + node.getWbsCode() + " · " + node.getWbsName() + "】";
            if (WbsNodeType.SUMMARY.matches(node.getNodeType())) {
                if (node.getChildCount() == null || node.getChildCount() == 0) {
                    issues.add(label + "为空，WBS分支必须最终落到工作包");
                }
                continue;
            }
            validateWorkPackageReadiness(project, node, label, issues);
        }
        for (ProjectWbsNode root : roots) {
            String label = "顶层WBS【" + root.getWbsCode() + " · " + root.getWbsName() + "】";
            if (root.getPlanStartDate() != null && root.getTargetStartDate() != null
                    && root.getPlanStartDate().isBefore(root.getTargetStartDate())) {
                issues.add(label + "汇总开始日期早于立项批准目标窗口");
            }
            if (root.getPlanEndDate() != null && root.getTargetEndDate() != null
                    && root.getPlanEndDate().isAfter(root.getTargetEndDate())) {
                issues.add(label + "汇总结束日期晚于立项批准目标窗口");
            }
        }
        return new StartReadinessResult(issues.isEmpty(), issues);
    }

    /** 校验工作包启动条件：负责人、验收定义、计划日期、必交交付物与任务。 */
    private void validateWorkPackageReadiness(ProjectInfo project, ProjectWbsNode workPackage, String label,
            List<String> issues) {
        if (workPackage.getOwnerId() == null) {
            issues.add(label + "未设置负责人");
        } else if (!teamService.isActiveMember(project.getProjectId(), workPackage.getOwnerId())) {
            issues.add(label + "负责人不属于当前在组成员");
        }
        if (StringUtils.isBlank(workPackage.getAcceptanceCriteria())) {
            issues.add(label + "未填写验收标准");
        }
        if (StringUtils.isBlank(workPackage.getDefinitionOfDone())) {
            issues.add(label + "未填写完成定义");
        }
        if (workPackage.getPlanStartDate() == null || workPackage.getPlanEndDate() == null) {
            issues.add(label + "未完整设置计划日期");
        } else {
            if (workPackage.getPlanEndDate().isBefore(workPackage.getPlanStartDate())) {
                issues.add(label + "计划结束日期早于开始日期");
            }
            if (workPackage.getPlanStartDate().isBefore(project.getStartDate())
                    || workPackage.getPlanEndDate().isAfter(project.getEndDate())) {
                issues.add(label + "计划日期超出项目周期");
            }
        }
        if ("1".equals(workPackage.getDeliverableRequired())
                && deliverableMapper.countRequiredByWorkPackageId(workPackage.getWbsId()) == 0) {
            issues.add(label + "至少需要一个必交正式交付物");
        }
        ProjectTask taskFilter = new ProjectTask();
        taskFilter.setWorkPackageId(workPackage.getWbsId());
        List<ProjectTask> tasks = taskMapper.selectList(taskFilter);
        Map<Long, ProjectTask> taskById = new HashMap<>();
        tasks.forEach(x -> taskById.put(x.getTaskId(), x));
        if (tasks.stream().noneMatch(x -> TaskType.EXECUTION.matches(x.getTaskType()))) {
            issues.add(label + "至少需要一个末级执行任务");
        }
        for (ProjectTask task : tasks) {
            String taskLabel = "任务【" + task.getTaskCode() + " · " + task.getTaskName() + "】";
            if (TaskType.SUMMARY.matches(task.getTaskType())) {
                if (task.getChildCount() == null || task.getChildCount() == 0) {
                    issues.add(taskLabel + "为汇总任务但没有下级任务");
                }
                continue;
            }
            if (task.getAssigneeId() == null) {
                issues.add(taskLabel + "未设置执行人");
            } else if (!teamService.isActiveMember(project.getProjectId(), task.getAssigneeId())) {
                issues.add(taskLabel + "执行人不属于当前在组成员");
            }
            if (task.getPlanStartDate() == null || task.getPlanEndDate() == null) {
                issues.add(taskLabel + "未完整设置计划日期");
                continue;
            }
            if (task.getPlanEndDate().isBefore(task.getPlanStartDate())) {
                issues.add(taskLabel + "计划结束日期早于开始日期");
            }
            if (workPackage.getPlanStartDate() != null && workPackage.getPlanEndDate() != null
                    && (task.getPlanStartDate().isBefore(workPackage.getPlanStartDate())
                            || task.getPlanEndDate().isAfter(workPackage.getPlanEndDate()))) {
                issues.add(taskLabel + "计划日期超出所属工作包");
            }
            if (task.getParentTaskId() != null && task.getParentTaskId() != 0) {
                ProjectTask parent = taskById.get(task.getParentTaskId());
                if (parent == null) {
                    issues.add(taskLabel + "上级任务不存在");
                } else if (parent.getPlanStartDate() != null && parent.getPlanEndDate() != null
                        && (task.getPlanStartDate().isBefore(parent.getPlanStartDate())
                                || task.getPlanEndDate().isAfter(parent.getPlanEndDate()))) {
                    issues.add(taskLabel + "计划日期超出上级任务");
                }
            }
        }
    }

    /** 查询项目初步计划列表。 */
    @Override
    public List<ProjectPreliminaryPlan> preliminaryPlans(Long projectId) {
        return projectMapper.selectPreliminaryPlans(projectId);
    }

    /** 新增初步计划。 */
    @Override
    public int addPreliminaryPlan(ProjectPreliminaryPlan plan, String operator) {
        ProjectInfo project = editable(plan.getProjectId(), operator);
        validatePlan(plan, project);
        plan.setCreateBy(operator);
        if (plan.getSortOrder() == null) {
            plan.setSortOrder(0);
        }
        return projectMapper.insertPreliminaryPlan(plan);
    }

    /** 修改初步计划，已转正式WBS的概要不允许修改。 */
    @Override
    public int updatePreliminaryPlan(ProjectPreliminaryPlan plan, String operator) {
        ProjectPreliminaryPlan old = projectMapper.selectPreliminaryPlan(plan.getPlanId());
        if (old == null) {
            throw new ServiceException("WBS概要不存在");
        }
        if (old.getConvertedWbsId() != null) {
            throw new ServiceException("该WBS概要已转为正式WBS，不能修改");
        }
        ProjectInfo project = editable(old.getProjectId(), operator);
        plan.setProjectId(old.getProjectId());
        validatePlan(plan, project);
        plan.setUpdateBy(operator);
        return projectMapper.updatePreliminaryPlan(plan);
    }

    /** 删除初步计划，已转正式WBS的概要不允许删除。 */
    @Override
    public int deletePreliminaryPlan(Long id, String operator) {
        ProjectPreliminaryPlan plan = projectMapper.selectPreliminaryPlan(id);
        if (plan == null) {
            throw new ServiceException("WBS概要不存在");
        }
        if (plan.getConvertedWbsId() != null) {
            throw new ServiceException("该WBS概要已转为正式WBS，不能删除");
        }
        editable(plan.getProjectId(), operator);
        return projectMapper.deletePreliminaryPlan(id);
    }

    /** 提交立项审批，校验立项材料完整性并生成审批快照。 */
    @Override
    @Transactional
    public int submitInitiation(Long id, String operator) {
        ProjectInfo project = editable(id, operator);
        List<ProjectPreliminaryPlan> plans = projectMapper.selectPreliminaryPlans(id);
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(project.getProjectBackground())) {
            missing.add("项目背景");
        }
        if (StringUtils.isBlank(project.getProjectScope())) {
            missing.add("项目范围");
        }
        if (StringUtils.isBlank(project.getExpectedOutcome())) {
            missing.add("预期成果");
        }
        if (StringUtils.isBlank(project.getResourceRequirement())) {
            missing.add("资源需求");
        }
        if (StringUtils.isBlank(project.getMajorRisk())) {
            missing.add("主要风险");
        }
        if (StringUtils.isBlank(project.getFeasibilityConclusion())) {
            missing.add("可行性综合结论");
        }
        if (plans.isEmpty()) {
            missing.add("WBS概要");
        }
        if (!missing.isEmpty()) {
            throw new ServiceException("请先完善立项材料：" + String.join("、", missing));
        }
        if ("1".equals(project.getBudgetRequired()) && project.getBudgetAmount() == null) {
            throw new ServiceException("项目需要预算，请填写预算总额");
        }
        int version = (project.getInitiationVersion() == null ? 0 : project.getInitiationVersion()) + 1;
        ProjectInitiationApproval approval = new ProjectInitiationApproval();
        approval.setProjectId(id);
        approval.setVersionNo(version);
        approval.setSubmitBy(operator);
        approval.setStatus(InitiationApprovalStatus.PENDING.getCode());
        ProjectMember teamFilter = new ProjectMember();
        teamFilter.setProjectId(id);
        teamFilter.setStatus(ProjectMemberStatus.ACTIVE.getCode());
        List<ProjectMember> team = teamService.members(teamFilter);
        List<ProjectInitiationAttachment> attachments = attachmentMapper.selectDraft(id, null);
        attachments.forEach(x -> x.setVersionNo(version));
        try {
            approval.setSnapshotJson(objectMapper.writeValueAsString(Map.of(
                    "project", project,
                    "team", team,
                    "wbsOutlines", plans,
                    "attachments", attachments)));
        } catch (Exception e) {
            throw new ServiceException("生成立项申请快照失败");
        }
        projectMapper.insertApproval(approval);
        attachmentMapper.bindDraft(id, approval.getApprovalId(), version);
        project.setStatus(ProjectStatus.PENDING_APPROVAL.getCode());
        project.setInitiationVersion(version);
        project.setUpdateBy(operator);
        return projectMapper.updateInitiationState(project);
    }

    /** 审批立项申请，通过后把WBS概要转为正式WBS。 */
    @Override
    @Transactional
    public int reviewInitiation(Long id, InitiationReviewRequest request, String operator) {
        if (!"admin".equalsIgnoreCase(operator)) {
            throw new ServiceException("只有admin可以审批立项申请");
        }
        ProjectInfo project = requiredProject(id);
        if (!ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            throw new ServiceException("项目不在立项审批中");
        }
        ProjectInitiationApproval approval = projectMapper.selectPendingApproval(id);
        if (approval == null) {
            throw new ServiceException("待审批记录不存在");
        }
        String result = request.getResult().trim().toUpperCase();
        boolean approved = InitiationApprovalStatus.APPROVED.matches(result);
        if (!approved && !InitiationApprovalStatus.RETURNED.matches(result)) {
            throw new ServiceException("审批结果不正确");
        }
        if (!approved && StringUtils.isBlank(request.getComment())) {
            throw new ServiceException("退回必须填写审批意见");
        }
        approval.setStatus(result);
        approval.setReviewBy(operator);
        approval.setReviewComment(request.getComment());
        int rows = projectMapper.reviewApproval(approval);
        project.setStatus(approved ? ProjectStatus.APPROVED.getCode() : ProjectStatus.DRAFT.getCode());
        project.setInitiationTime(approved ? LocalDateTime.now() : null);
        project.setUpdateBy(operator);
        projectMapper.updateInitiationState(project);
        if (approved) {
            convertPlans(project, operator);
        } else {
            // 退回后复制为新的草稿记录，保证原审批版本的附件不可变且草稿可编辑。
            attachmentMapper.copyToDraft(approval.getApprovalId(), approval.getVersionNo(), operator);
        }
        return rows;
    }

    /** 查询立项审批历史。 */
    @Override
    public List<ProjectInitiationApproval> approvalHistory(Long id) {
        return projectMapper.selectApprovals(id);
    }

    /** 查询立项审批快照。 */
    @Override
    public ProjectInitiationApproval approvalSnapshot(Long projectId, Long approvalId) {
        return projectMapper.selectApprovals(projectId).stream()
                .filter(x -> x.getApprovalId().equals(approvalId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("审批记录不存在"));
    }

    /** 查询当前立项申请附件；草稿使用未绑定记录，审批中/已立项使用对应版本记录。 */
    @Override
    public List<ProjectInitiationAttachment> initiationAttachments(Long projectId, String sectionCode) {
        ProjectInfo project = requiredProject(projectId);
        if (ProjectStatus.DRAFT.matches(project.getStatus())) {
            return attachmentMapper.selectDraft(projectId, sectionCode);
        }
        if (ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            ProjectInitiationApproval pending = projectMapper.selectPendingApproval(projectId);
            return pending == null ? List.of() : attachmentMapper.selectByApproval(pending.getApprovalId(), sectionCode);
        }
        return attachmentMapper.selectLatestApproved(projectId, sectionCode);
    }

    /** 查询指定审批版本附件，供审批记录查看。 */
    @Override
    public List<ProjectInitiationAttachment> initiationApprovalAttachments(Long projectId, Long approvalId,
            String sectionCode) {
        boolean exists = projectMapper.selectApprovals(projectId).stream()
                .anyMatch(x -> approvalId.equals(x.getApprovalId()));
        if (!exists) {
            throw new ServiceException("审批记录不存在");
        }
        return attachmentMapper.selectByApproval(approvalId, sectionCode);
    }

    /** 新增当前草稿附件。 */
    @Override
    public int addInitiationAttachment(ProjectInitiationAttachment attachment, String operator) {
        ProjectInfo project = editable(attachment.getProjectId(), operator);
        validateAttachmentSection(attachment.getSectionCode());
        attachment.setApprovalId(null);
        attachment.setVersionNo(0);
        attachment.setUploadBy(operator);
        return attachmentMapper.insert(attachment);
    }

    /** 删除当前草稿附件。 */
    @Override
    public int deleteInitiationAttachment(Long projectId, Long attachmentId, String operator) {
        editable(projectId, operator);
        int rows = attachmentMapper.deleteDraft(projectId, attachmentId);
        if (rows == 0) {
            throw new ServiceException("附件不存在或已进入审批版本，不能删除");
        }
        return rows;
    }

    private void validateAttachmentSection(String sectionCode) {
        if (!List.of("BASIC_SCHEME", "RESOURCE_BUDGET", "RISK_ASSESSMENT").contains(sectionCode)) {
            throw new ServiceException("附件所属页签不正确");
        }
    }

    /** 立项通过后把WBS概要转换为正式WBS节点。 */
    private void convertPlans(ProjectInfo project, String operator) {
        int code = nextTopLevelWbsCode(project.getProjectId());
        for (ProjectPreliminaryPlan plan : projectMapper.selectPreliminaryPlans(project.getProjectId())) {
            if (plan.getConvertedWbsId() != null) {
                continue;
            }
            ProjectWbsNode wbs = new ProjectWbsNode();
            wbs.setProjectId(project.getProjectId());
            wbs.setParentId(0L);
            wbs.setWbsCode(String.valueOf(code++));
            wbs.setNodeType(WbsNodeType.SUMMARY.getCode());
            wbs.setWbsName(plan.getOutlineName());
            wbs.setScopeDescription(plan.getOutlineDescription());
            wbs.setTargetStartDate(plan.getStartDate());
            wbs.setTargetEndDate(plan.getEndDate());
            wbs.setTargetMilestone(plan.getMilestoneName());
            wbs.setStatus(WbsStatus.NOT_STARTED.getCode());
            wbs.setProgress(0);
            // 顶层WBS概要默认不需要正式交付物
            wbs.setDeliverableRequired("0");
            wbs.setSortOrder(plan.getSortOrder());
            wbs.setCreateBy(operator);
            wbsMapper.insert(wbs);
            projectMapper.markPlanConverted(plan.getPlanId(), wbs.getWbsId());
        }
    }

    /** 获取项目中下一个可用的顶层WBS数字编码，兼容审批前已经编制的范围节点。 */
    private int nextTopLevelWbsCode(Long projectId) {
        int maxCode = 0;
        for (ProjectWbsNode node : wbsMapper.selectChildren(projectId, 0L)) {
            try {
                maxCode = Math.max(maxCode, Integer.parseInt(node.getWbsCode()));
            } catch (NumberFormatException ignored) {
                // 顶层WBS由系统生成纯数字编码；历史异常编码不参与编号计算，避免阻塞立项。
            }
        }
        return maxCode + 1;
    }

    /** 校验操作者可编辑且项目处于申请草稿状态。 */
    private ProjectInfo editable(Long id, String operator) {
        ProjectInfo project = requiredProject(id);
        if (!ProjectStatus.DRAFT.matches(project.getStatus())) {
            throw new ServiceException("只有申请草稿可以修改或提交");
        }
        return project;
    }

    /** 校验初步计划日期在项目周期内。 */
    private void validatePlan(ProjectPreliminaryPlan plan, ProjectInfo project) {
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new ServiceException("WBS概要结束日期不能早于开始日期");
        }
        if (plan.getStartDate().isBefore(project.getStartDate())
                || plan.getEndDate().isAfter(project.getEndDate())) {
            throw new ServiceException("WBS概要目标日期必须在项目预计日期范围内（" + project.getStartDate() + " ~ "
                    + project.getEndDate() + "）");
        }
    }

    /** 查询项目，不存在时抛异常。 */
    private ProjectInfo requiredProject(Long id) {
        ProjectInfo project = projectMapper.selectProjectInfoById(id);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        return project;
    }

    /** 校验项目基本信息：日期、分类、负责人与状态默认值。 */
    private void validate(ProjectInfo project) {
        if (project.getEndDate().isBefore(project.getStartDate())) {
            throw new ServiceException("计划结束日期不能早于开始日期");
        }
        if (categoryMapper.selectProjectCategoryById(project.getCategoryId()) == null) {
            throw new ServiceException("项目分类不存在");
        }
        ProjectPerson manager = personMapper.selectProjectPersonById(project.getManagerId());
        if (manager == null || !"0".equals(manager.getStatus())) {
            throw new ServiceException("请选择启用状态的项目负责人");
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (StringUtils.isBlank(project.getStatus())) {
            project.setStatus(ProjectStatus.DRAFT.getCode());
        }
    }
}
