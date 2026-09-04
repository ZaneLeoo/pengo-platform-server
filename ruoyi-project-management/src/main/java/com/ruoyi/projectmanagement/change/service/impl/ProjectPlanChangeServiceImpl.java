package com.ruoyi.projectmanagement.change.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCostAggregate;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.mapper.ProjectActualCostMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.change.domain.*;
import com.ruoyi.projectmanagement.change.mapper.ProjectPlanChangeMapper;
import com.ruoyi.projectmanagement.change.service.IProjectPlanChangeService;
import com.ruoyi.projectmanagement.change.service.ProjectPlanChangeAuditService;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableTypeMapper;
import com.ruoyi.projectmanagement.issue.mapper.ProjectIssueMapper;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import com.ruoyi.projectmanagement.professionalrole.mapper.ProfessionalRoleMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.domain.ProjectRole;
import com.ruoyi.projectmanagement.team.mapper.ProjectTeamMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import com.ruoyi.projectmanagement.workflow.service.WorkflowBusinessCallback;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 计划基线与变更单的最小闭环；具体计划应用操作在变更项校验后事务执行。 */
@Service
public class ProjectPlanChangeServiceImpl
        implements IProjectPlanChangeService, WorkflowBusinessCallback {
    private final ProjectPlanChangeMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final IProjectTeamService teamService;
    private final ObjectMapper objectMapper;
    private final IWorkflowService workflowService;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectTaskMapper taskMapper;
    private final ProjectDeliverableMapper deliverableMapper;
    private final ProjectDeliverableTypeMapper deliverableTypeMapper;
    private final ProjectTeamMapper teamMapper;
    private final ProjectPlanChangeAuditService auditService;
    private final ProjectIssueMapper issueMapper;
    private final IProjectTaskService taskService;
    private final IProjectWbsService wbsService;
    private final ProjectCategoryMapper categoryMapper;
    private final ProjectPersonMapper personMapper;
    private final ProfessionalRoleMapper professionalRoleMapper;
    private final ProjectBudgetMapper budgetMapper;
    private final ProjectWorkPackageBudgetMapper workPackageBudgetMapper;
    private final ProjectActualCostMapper actualCostMapper;
    private final ICostCategoryService costCategoryService;

    /** Fields maintained by the runtime rather than by a plan baseline change. */
    private static final Set<String> BASELINE_COMPARE_IGNORED_FIELDS =
            Set.of(
                    "createBy",
                    "createTime",
                    "updateBy",
                    "updateTime",
                    "status",
                    "progress",
                    "remark",
                    "actualStartDate",
                    "actualEndDate",
                    "childCount",
                    "taskCount",
                    "deliverableCount",
                    "completedTaskCount",
                    "overdueTaskCount",
                    "allowedExtensions",
                    // 交付物提交、审批属于执行留痕，不应在计划基线比较中被误判为计划变更。
                    "submitBy",
                    "latestFileUrl",
                    "latestExternalUrl",
                    "reviewerName",
                    "projectStatus");

    public ProjectPlanChangeServiceImpl(
            ProjectPlanChangeMapper mapper,
            ProjectInfoMapper projectMapper,
            IProjectTeamService teamService,
            ObjectMapper objectMapper,
            @Lazy IWorkflowService workflowService,
            ProjectWbsMapper wbsMapper,
            ProjectTaskMapper taskMapper,
            ProjectDeliverableMapper deliverableMapper,
            ProjectDeliverableTypeMapper deliverableTypeMapper,
            ProjectTeamMapper teamMapper,
            ProjectPlanChangeAuditService auditService,
            ProjectIssueMapper issueMapper,
            IProjectTaskService taskService,
            IProjectWbsService wbsService,
            ProjectCategoryMapper categoryMapper,
            ProjectPersonMapper personMapper,
            ProfessionalRoleMapper professionalRoleMapper,
            ProjectBudgetMapper budgetMapper,
            ProjectWorkPackageBudgetMapper workPackageBudgetMapper,
            ProjectActualCostMapper actualCostMapper,
            ICostCategoryService costCategoryService) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.teamService = teamService;
        this.objectMapper = objectMapper;
        this.workflowService = workflowService;
        this.wbsMapper = wbsMapper;
        this.taskMapper = taskMapper;
        this.deliverableMapper = deliverableMapper;
        this.deliverableTypeMapper = deliverableTypeMapper;
        this.teamMapper = teamMapper;
        this.auditService = auditService;
        this.issueMapper = issueMapper;
        this.taskService = taskService;
        this.wbsService = wbsService;
        this.categoryMapper = categoryMapper;
        this.personMapper = personMapper;
        this.professionalRoleMapper = professionalRoleMapper;
        this.budgetMapper = budgetMapper;
        this.workPackageBudgetMapper = workPackageBudgetMapper;
        this.actualCostMapper = actualCostMapper;
        this.costCategoryService = costCategoryService;
    }

    @Override
    public String businessType() {
        return "PLAN_CHANGE";
    }

    @Override
    @Transactional
    public void createInitialBaseline(Long projectId, String operator) {
        if (mapper.selectCurrentBaseline(projectId) != null) return;
        ProjectInfo project = requireProject(projectId);
        try {
            ProjectPlanBaseline baseline = new ProjectPlanBaseline();
            baseline.setProjectId(projectId);
            baseline.setVersionNo(1);
            baseline.setCreateBy(operator);
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("project", project);
            ProjectWbsNode wbsFilter = new ProjectWbsNode();
            wbsFilter.setProjectId(projectId);
            snapshot.put("wbs", wbsMapper.selectList(wbsFilter));
            ProjectTask taskFilter = new ProjectTask();
            taskFilter.setProjectId(projectId);
            snapshot.put("tasks", taskMapper.selectList(taskFilter));
            ProjectDeliverable deliverableFilter = new ProjectDeliverable();
            deliverableFilter.setProjectId(projectId);
            snapshot.put("deliverables", deliverableMapper.selectList(deliverableFilter));
            ProjectMember teamFilter = new ProjectMember();
            teamFilter.setProjectId(projectId);
            teamFilter.setStatus("ACTIVE");
            snapshot.put("team", teamService.members(teamFilter));
            snapshot.put("projectBudget", budgetMapper.selectByProjectId(projectId));
            snapshot.put("workPackageBudget", workPackageBudgetMapper.selectByProjectId(projectId));
            baseline.setSnapshotJson(objectMapper.writeValueAsString(snapshot));
            mapper.insertBaseline(baseline);
        } catch (Exception e) {
            throw new ServiceException("创建项目初始基线失败");
        }
    }

    @Override
    public List<ProjectPlanBaseline> baselines(Long projectId, Long userId) {
        assertProjectMemberOrManager(projectId, userId);
        return mapper.selectBaselines(projectId);
    }

    @Override
    public Map<String, Object> compare(
            Long projectId, Long fromBaselineId, Long toBaselineId, Long userId) {
        assertProjectMemberOrManager(projectId, userId);
        ProjectPlanBaseline from = mapper.selectBaseline(fromBaselineId),
                to = mapper.selectBaseline(toBaselineId);
        if (from == null
                || to == null
                || !projectId.equals(from.getProjectId())
                || !projectId.equals(to.getProjectId()))
            throw new ServiceException("基线不存在或不属于当前项目");
        try {
            Map<String, Object> left =
                    objectMapper.readValue(
                            from.getSnapshotJson(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> right =
                    objectMapper.readValue(
                            to.getSnapshotJson(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fromVersion", from.getVersionNo());
            result.put("toVersion", to.getVersionNo());
            Map<String, Object> modules = new LinkedHashMap<>();
            modules.put("PROJECT_INFO", compareSingle(left.get("project"), right.get("project")));
            modules.put(
                    "PROJECT_BUDGET",
                    compareCollection(
                            left.get("projectBudget"),
                            right.get("projectBudget"),
                            "costCategoryId",
                            "categoryPath"));
            modules.put(
                    "WORK_PACKAGE_BUDGET",
                    compareCollection(
                            left.get("workPackageBudget"),
                            right.get("workPackageBudget"),
                            "workPackageBudgetLineId",
                            "workPackageName"));
            modules.put(
                    "WBS",
                    compareCollection(left.get("wbs"), right.get("wbs"), "wbsId", "wbsName"));
            modules.put(
                    "TASK",
                    compareCollection(left.get("tasks"), right.get("tasks"), "taskId", "taskName"));
            modules.put(
                    "DELIVERABLE",
                    compareCollection(
                            left.get("deliverables"),
                            right.get("deliverables"),
                            "deliverableId",
                            "deliverableName"));
            modules.put(
                    "TEAM",
                    compareCollection(
                            left.get("team"), right.get("team"), "memberId", "personName"));
            result.put("modules", modules);
            return result;
        } catch (Exception e) {
            throw new ServiceException("基线快照比较失败");
        }
    }

    private Map<String, Object> compareSingle(Object before, Object after) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> modified = new ArrayList<>();
        Map<String, Object> beforeValue = baselineCompareValue(before);
        Map<String, Object> afterValue = baselineCompareValue(after);
        if (before != null && after != null && !beforeValue.equals(afterValue)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("before", beforeValue);
            row.put("after", afterValue);
            modified.add(row);
        }
        result.put("added", List.of());
        result.put("removed", List.of());
        result.put("modified", modified);
        return result;
    }

    private Map<String, Object> compareCollection(
            Object before, Object after, String idKey, String nameKey) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Map<String, Object>> left = indexCollection(before, idKey),
                right = indexCollection(after, idKey);
        List<Object> added = new ArrayList<>(),
                removed = new ArrayList<>(),
                modified = new ArrayList<>();
        right.forEach(
                (id, value) -> {
                    Map<String, Object> afterValue = baselineCompareValue(value);
                    if (!left.containsKey(id)) added.add(afterValue);
                    else if (!afterValue.equals(baselineCompareValue(left.get(id)))) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("name", afterValue.get(nameKey));
                        row.put("before", baselineCompareValue(left.get(id)));
                        row.put("after", afterValue);
                        modified.add(row);
                    }
                });
        left.forEach(
                (id, value) -> {
                    if (!right.containsKey(id)) removed.add(baselineCompareValue(value));
                });
        result.put("added", added);
        result.put("removed", removed);
        result.put("modified", modified);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> baselineCompareValue(Object value) {
        if (!(value instanceof Map<?, ?> source)) return Map.of();
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach(
                (key, fieldValue) -> {
                    String field = String.valueOf(key);
                    if (!BASELINE_COMPARE_IGNORED_FIELDS.contains(field)) {
                        result.put(field, fieldValue);
                    }
                });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> indexCollection(Object value, String idKey) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        if (value instanceof List<?> list) {
            for (Object row : list) {
                if (row instanceof Map<?, ?> map && map.get(idKey) != null)
                    result.put(String.valueOf(map.get(idKey)), (Map<String, Object>) map);
            }
        }
        return result;
    }

    @Override
    public List<ProjectPlanChange> list(Long projectId, Long userId) {
        requireProject(projectId);
        if (!teamService.isActiveMember(projectId, userId) && !isManager(projectId, userId))
            throw new ServiceException("您无权查看该项目变更");
        List<ProjectPlanChange> rows = mapper.selectChanges(projectId);
        rows.forEach(x -> enrich(x, userId));
        return rows;
    }

    @Override
    public List<ProjectPlanChange> page(ProjectPlanChange query, Long userId) {
        if (query == null || query.getProjectId() == null) {
            throw new ServiceException("请选择项目");
        }
        assertProjectMemberOrManager(query.getProjectId(), userId);
        List<ProjectPlanChange> rows = mapper.selectChangesPage(query);
        rows.forEach(item -> enrich(item, userId));
        return rows;
    }

    @Override
    public List<ProjectPlanChangeProjectNavigator> navigatorProjects(
            String keyword, boolean includeHistory, Long userId) {
        return mapper.selectNavigatorProjects(
                userId, keyword == null ? null : keyword.trim(), includeHistory);
    }

    @Override
    public ProjectPlanChangeProjectCapability capability(Long projectId, Long userId) {
        ProjectPlanChangeProjectCapability capability = new ProjectPlanChangeProjectCapability();
        if (!teamService.isActiveMember(projectId, userId)) {
            capability.setReadonlyReason("您不是项目活动成员");
            return capability;
        }
        ProjectInfo project = requireProject(projectId);
        if (ProjectStatus.ACTIVE.matches(project.getStatus())
                || ProjectStatus.PAUSED.matches(project.getStatus())) {
            capability.setCanCreate(true);
            return capability;
        }
        capability.setReadonlyReason(
                ProjectStatus.APPROVED.matches(project.getStatus())
                        ? "项目启动后会自动生成 V1 基线；启动前请直接维护项目计划"
                        : "当前项目状态仅允许查看历史基线和变更记录");
        return capability;
    }

    @Override
    public List<ProjectMember> memberCandidates(Long projectId, String keyword, Long userId) {
        requireProject(projectId);
        assertProjectMemberOrManager(projectId, userId);
        if (keyword == null || keyword.trim().length() < 2) return List.of();
        return mapper.selectMemberCandidates(projectId, keyword.trim());
    }

    @Override
    public ProjectPlanChange detail(Long changeId, Long userId) {
        ProjectPlanChange c = require(changeId);
        assertChangeViewable(c, userId);
        enrich(c, userId);
        return c;
    }

    @Override
    public ProjectPlanChangeAttachment attachment(Long changeId, Long attachmentId, Long userId) {
        ProjectPlanChange c = require(changeId);
        assertChangeViewable(c, userId);
        return mapper.selectAttachments(changeId).stream()
                .filter(x -> attachmentId != null && attachmentId.equals(x.getAttachmentId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("变更附件不存在"));
    }

    @Override
    @Transactional
    public Long save(ProjectPlanChange change, String operator, Long userId) {
        if (change == null) throw new ServiceException("变更单不能为空");
        ProjectPlanChange old = change.getChangeId() == null ? null : require(change.getChangeId());
        Long projectId = old == null ? change.getProjectId() : old.getProjectId();
        if (projectId == null || (old != null && !projectId.equals(change.getProjectId())))
            throw new ServiceException("变更单所属项目不能修改");
        change.setProjectId(projectId);
        assertChangeable(projectId, userId);
        if (change.getTitle() == null
                || change.getTitle().isBlank()
                || change.getChangeReason() == null
                || change.getChangeReason().isBlank()
                || change.getImpactDescription() == null
                || change.getImpactDescription().isBlank())
            throw new ServiceException("标题、变更原因和影响说明不能为空");
        ProjectPlanBaseline base = mapper.selectCurrentBaseline(projectId);
        if (base == null) throw new ServiceException("项目尚未建立计划基线");
        if (change.getChangeId() == null) {
            change.setBaseBaselineId(base.getBaselineId());
            change.setBaseVersionNo(base.getVersionNo());
            change.setStatus("DRAFT");
            change.setApplicantUserId(userId);
            change.setCreateBy(operator);
            change.setChangeCode("PC-" + change.getProjectId() + "-" + System.currentTimeMillis());
            mapper.insertChange(change);
            audit(change.getChangeId(), "CREATED", userId, operator, "创建变更草稿");
        } else {
            if (old.getApplicantUserId() == null
                    || !old.getApplicantUserId().equals(userId)
                    || !("DRAFT".equals(old.getStatus())
                            || "RETURNED".equals(old.getStatus())
                            || "RECONCILE".equals(old.getStatus())))
                throw new ServiceException("当前变更单不能编辑");
            change.setStatus(old.getStatus());
            if ("RECONCILE".equals(old.getStatus())) {
                change.setBaseBaselineId(base.getBaselineId());
                change.setBaseVersionNo(base.getVersionNo());
            } else {
                change.setBaseBaselineId(old.getBaseBaselineId());
                change.setBaseVersionNo(old.getBaseVersionNo());
            }
            change.setApplicantUserId(old.getApplicantUserId());
            change.setWorkflowInstanceId(old.getWorkflowInstanceId());
            change.setUpdateBy(operator);
            mapper.updateChange(change);
            mapper.deleteItems(change.getChangeId());
            mapper.deleteAttachments(change.getChangeId());
            audit(change.getChangeId(), "UPDATED", userId, operator, "编辑变更草稿");
        }
        validateItems(change.getProjectId(), change.getItems());
        validateAttachments(change.getAttachments());
        for (ProjectPlanChangeItem i :
                change.getItems() == null ? List.<ProjectPlanChangeItem>of() : change.getItems()) {
            i.setChangeId(change.getChangeId());
            if (i.getBeforeJson() != null && i.getBeforeJson().isBlank()) {
                i.setBeforeJson(null);
            }
            if (i.getAfterJson() != null && i.getAfterJson().isBlank()) {
                i.setAfterJson(null);
            }
            mapper.insertItem(i);
        }
        for (ProjectPlanChangeAttachment a :
                change.getAttachments() == null
                        ? List.<ProjectPlanChangeAttachment>of()
                        : change.getAttachments()) {
            a.setChangeId(change.getChangeId());
            mapper.insertAttachment(a);
        }
        return change.getChangeId();
    }

    @Override
    @Transactional
    public void submit(Long id, String op, Long user) {
        ProjectPlanChange c = require(id);
        if (!c.getApplicantUserId().equals(user)
                || !("DRAFT".equals(c.getStatus())
                        || "RETURNED".equals(c.getStatus())
                        || "RECONCILE".equals(c.getStatus())))
            throw new ServiceException("当前变更单不能提交");
        assertChangeable(c.getProjectId(), user);
        List<ProjectPlanChangeItem> items = mapper.selectItems(id);
        if (items.isEmpty()) throw new ServiceException("请至少添加一条变更项");
        validateItems(c.getProjectId(), items);
        validateFinalBudget(c.getProjectId(), items);
        validateAttachments(mapper.selectAttachments(id));
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("change", c);
            snapshot.put("items", items);
            snapshot.put("attachments", mapper.selectAttachments(id));
            Long instance =
                    workflowService.start(
                            businessType(),
                            id,
                            c.getProjectId(),
                            "项目变更：" + c.getTitle(),
                            objectMapper.writeValueAsString(snapshot),
                            op,
                            user);
            c.setStatus("PENDING");
            c.setUpdateBy(op);
            mapper.updateChange(c);
            mapper.bindWorkflow(id, instance);
            audit(id, "SUBMITTED", user, op, "提交项目计划变更审批");
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("生成变更审批快照失败");
        }
    }

    @Override
    @Transactional
    public void delete(Long id, String op, Long user) {
        ProjectPlanChange c = require(id);
        if (!c.getApplicantUserId().equals(user)
                || !("DRAFT".equals(c.getStatus())
                        || "RETURNED".equals(c.getStatus())
                        || "RECONCILE".equals(c.getStatus())))
            throw new ServiceException("当前变更单不能删除");
        assertChangeable(c.getProjectId(), user);
        audit(id, "DELETED", user, op, "删除变更单");
        mapper.deleteItems(id);
        mapper.deleteAttachments(id);
        mapper.deleteChange(id);
    }

    @Override
    @Transactional
    public void withdraw(Long id, String op, Long user) {
        ProjectPlanChange c = require(id);
        if (!c.getApplicantUserId().equals(user) || !"PENDING".equals(c.getStatus()))
            throw new ServiceException("当前变更单不能撤回");
        assertChangeable(c.getProjectId(), user);
        workflowService.withdraw(c.getWorkflowInstanceId(), op, user);
        c.setStatus("WITHDRAWN");
        c.setUpdateBy(op);
        mapper.updateChange(c);
        audit(id, "WITHDRAWN", user, op, "发起人撤回审批");
    }

    @Override
    @Transactional(noRollbackFor = PlanChangeReconcileException.class)
    public void apply(Long id, String op, Long user) {
        ProjectPlanChange c = requireForUpdate(id);
        try {
            ProjectInfo p = requireProject(c.getProjectId());
            if (p.getManagerId() == null
                    || !p.getManagerId().equals(user)
                    || !"PENDING_APPLY".equals(c.getStatus()))
                throw new ServiceException("仅项目负责人可确认应用已审批变更");
            if (!ProjectStatus.ACTIVE.matches(p.getStatus())
                    && !ProjectStatus.PAUSED.matches(p.getStatus()))
                throw new ServiceException("仅执行中或暂停中项目可应用计划变更");
            ProjectPlanBaseline current = mapper.selectCurrentBaselineForUpdate(c.getProjectId());
            if (current == null || !current.getBaselineId().equals(c.getBaseBaselineId())) {
                c.setStatus("RECONCILE");
                c.setUpdateBy(op);
                mapper.updateChange(c);
                audit(id, "RECONCILE", user, op, "当前基线已变化，需要重新核对");
                throw new PlanChangeReconcileException("当前计划基线已变化，请重新核对变更单");
            }
            List<ProjectPlanChangeItem> items = mapper.selectItems(id);
            validateItems(c.getProjectId(), items);
            validateFinalBudget(c.getProjectId(), items);
            Map<Long, Long> appliedWorkPackages = new LinkedHashMap<>();
            for (ProjectPlanChangeItem item : orderedApplyItems(items))
                applyItem(c, item, op, appliedWorkPackages);
            refreshAggregates(c.getProjectId());
            createNextBaseline(c.getProjectId(), id, op, current.getVersionNo() + 1);
            c.setStatus("APPLIED");
            c.setUpdateBy(op);
            mapper.updateChange(c);
            audit(id, "APPLIED", user, op, "确认应用变更并生成新基线");
        } catch (Exception e) {
            String detail =
                    e instanceof ServiceException && e.getMessage() != null
                            ? e.getMessage()
                            : "应用变更失败";
            if (!(e instanceof PlanChangeReconcileException))
                auditService.recordApplyFailure(id, user, op, detail);
            if (e instanceof PlanChangeReconcileException) throw (PlanChangeReconcileException) e;
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException(detail);
        }
    }

    @Override
    public void approved(Long id, String op, String opinion) {
        approved(id, op, opinion, null, null);
    }

    @Override
    public void approved(Long id, String op, String opinion, Long approverId, Long instanceId) {
        ProjectPlanChange c = require(id);
        if (!"PENDING".equals(c.getStatus())
                || (instanceId != null
                        && c.getWorkflowInstanceId() != null
                        && !instanceId.equals(c.getWorkflowInstanceId()))) return;
        c.setStatus("PENDING_APPLY");
        c.setUpdateBy(op);
        mapper.updateChange(c);
        audit(id, "APPROVED", approverId, op, opinion == null ? "审批通过" : opinion);
    }

    @Override
    public void rejected(Long id, String op, String opinion) {
        rejected(id, op, opinion, null, null);
    }

    @Override
    public void rejected(Long id, String op, String opinion, Long approverId, Long instanceId) {
        ProjectPlanChange c = require(id);
        if (!"PENDING".equals(c.getStatus())
                || (instanceId != null
                        && c.getWorkflowInstanceId() != null
                        && !instanceId.equals(c.getWorkflowInstanceId()))) return;
        c.setStatus("RETURNED");
        c.setUpdateBy(op);
        mapper.updateChange(c);
        audit(id, "REJECTED", approverId, op, opinion == null ? "审批退回" : opinion);
    }

    private ProjectPlanChange require(Long id) {
        ProjectPlanChange c = mapper.selectChange(id);
        if (c == null) throw new ServiceException("项目变更单不存在");
        return c;
    }

    private ProjectPlanChange requireForUpdate(Long id) {
        ProjectPlanChange c = mapper.selectChangeForUpdate(id);
        if (c == null) throw new ServiceException("项目变更单不存在");
        return c;
    }

    private ProjectInfo requireProject(Long id) {
        ProjectInfo p = projectMapper.selectProjectInfoById(id);
        if (p == null) throw new ServiceException("项目不存在");
        return p;
    }

    private void assertMember(Long id, Long user) {
        if (!teamService.isActiveMember(id, user)) throw new ServiceException("您不是项目活动成员");
    }

    private boolean isManager(Long id, Long user) {
        ProjectInfo p = requireProject(id);
        return p.getManagerId() != null && p.getManagerId().equals(user);
    }

    private void assertProjectMemberOrManager(Long id, Long user) {
        if (!teamService.isActiveMember(id, user) && !isManager(id, user))
            throw new ServiceException("您无权查看该项目变更");
    }

    private void assertChangeViewable(ProjectPlanChange c, Long user) {
        if (teamService.isActiveMember(c.getProjectId(), user) || isManager(c.getProjectId(), user))
            return;
        if (c.getWorkflowInstanceId() != null) {
            try {
                workflowService.instanceDetail(c.getWorkflowInstanceId(), user);
                return;
            } catch (Exception ignored) {
                /* 非当前审批参与人继续拒绝 */
            }
        }
        throw new ServiceException("您无权查看该项目变更");
    }

    private void assertChangeable(Long id, Long user) {
        assertMember(id, user);
        ProjectInfo p = requireProject(id);
        if (!ProjectStatus.ACTIVE.matches(p.getStatus())
                && !ProjectStatus.PAUSED.matches(p.getStatus()))
            throw new ServiceException("仅执行中或暂停中项目可发起变更");
    }

    private void enrich(ProjectPlanChange c, Long user) {
        c.setItems(mapper.selectItems(c.getChangeId()));
        c.setAttachments(mapper.selectAttachments(c.getChangeId()));
        List<ProjectPlanChangeAudit> audits = mapper.selectAudits(c.getChangeId());
        c.setAudits(audits);
        audits.stream()
                .filter(x -> "SUBMITTED".equals(x.getAction()))
                .map(ProjectPlanChangeAudit::getCreateTime)
                .reduce((first, latest) -> latest)
                .ifPresent(c::setSubmitTime);
        audits.stream()
                .filter(x -> "APPLIED".equals(x.getAction()))
                .map(ProjectPlanChangeAudit::getCreateTime)
                .reduce((first, latest) -> latest)
                .ifPresent(c::setApplyTime);
        ProjectPlanChangeCapability cap = new ProjectPlanChangeCapability();
        ProjectInfo project = requireProject(c.getProjectId());
        boolean own = user != null && user.equals(c.getApplicantUserId()),
                manager =
                        user != null
                                && project.getManagerId() != null
                                && project.getManagerId().equals(user);
        boolean editable =
                own
                        && ("DRAFT".equals(c.getStatus())
                                || "RETURNED".equals(c.getStatus())
                                || "RECONCILE".equals(c.getStatus()));
        cap.setCanEdit(editable);
        cap.setCanDelete(editable);
        cap.setCanSubmit(editable);
        cap.setCanWithdraw(own && "PENDING".equals(c.getStatus()));
        cap.setCanApply(manager && "PENDING_APPLY".equals(c.getStatus()));
        List<String> actions = new ArrayList<>();
        if (cap.isCanEdit()) actions.add("EDIT");
        if (cap.isCanDelete()) actions.add("DELETE");
        if (cap.isCanSubmit()) actions.add("SUBMIT");
        if (cap.isCanWithdraw()) actions.add("WITHDRAW");
        if (cap.isCanApply()) actions.add("APPLY");
        cap.setAllowedActions(actions);
        c.setCapability(cap);
    }

    private void audit(Long changeId, String action, Long userId, String operator, String detail) {
        ProjectPlanChangeAudit audit = new ProjectPlanChangeAudit();
        audit.setChangeId(changeId);
        audit.setAction(action);
        audit.setOperatorUserId(userId);
        audit.setOperator(operator);
        audit.setDetail(detail);
        mapper.insertAudit(audit);
    }

    private void validateItems(Long projectId, List<ProjectPlanChangeItem> items) {
        if (items == null || items.isEmpty()) throw new ServiceException("请至少添加一条变更项");
        for (ProjectPlanChangeItem item : items) {
            if (item == null || item.getModuleType() == null || item.getOperationType() == null)
                throw new ServiceException("变更项缺少模块或操作类型");
            String module = item.getModuleType(), operation = item.getOperationType();
            if (!List.of(
                            "PROJECT_INFO",
                            "PROJECT_BUDGET",
                            "WORK_PACKAGE_BUDGET",
                            "WBS",
                            "TASK",
                            "DELIVERABLE",
                            "TEAM")
                    .contains(module)) throw new ServiceException("变更模块不受支持");
            if (!List.of("ADD", "UPDATE", "DELETE").contains(operation))
                throw new ServiceException("变更项操作类型无效");
            if ("PROJECT_INFO".equals(module) && !"UPDATE".equals(operation))
                throw new ServiceException("项目基础信息仅支持修改");
            if ("PROJECT_BUDGET".equals(module)
                    && "PROJECT_BUDGET_HEADER".equals(item.getTargetType())
                    && !"UPDATE".equals(operation)) throw new ServiceException("项目预算头仅支持修改");
            if (item.getTargetType() == null || item.getTargetType().isBlank())
                throw new ServiceException("变更项缺少目标类型");
            if (!"ADD".equals(operation)
                    && !("PROJECT_INFO".equals(module) && "UPDATE".equals(operation))
                    && (item.getTargetId() == null || item.getTargetId() <= 0)) {
                throw new ServiceException("修改或删除变更项必须指定变更对象");
            }
            if (item.getTargetName() == null || item.getTargetName().isBlank())
                throw new ServiceException("变更项必须填写目标名称");
            if (!"DELETE".equals(operation)
                    && (item.getAfterJson() == null || item.getAfterJson().isBlank()))
                throw new ServiceException("新增或修改变更项必须填写变更后内容");
            if (item.getItemReason() == null || item.getItemReason().isBlank())
                throw new ServiceException("每项变更必须填写变更说明");
            if (!"DELETE".equals(operation)) {
                try {
                    Map<String, Object> after =
                            objectMapper.readValue(item.getAfterJson(), Map.class);
                    if ("PROJECT_INFO".equals(module)) validateProjectInfoAfter(after);
                    if ("PROJECT_BUDGET".equals(module)) validateBudgetItem(projectId, item, after);
                    if ("WORK_PACKAGE_BUDGET".equals(module))
                        validateWorkPackageBudgetItem(projectId, item, after);
                    if ("WBS".equals(module)) validateWbsItem(projectId, item, after);
                    if ("TASK".equals(module)) validateTaskItem(projectId, item, after, Map.of());
                    if ("DELIVERABLE".equals(module))
                        validateDeliverableItem(projectId, item, after);
                    if ("TEAM".equals(module)) validateTeamItem(projectId, item, after);
                } catch (Exception e) {
                    if (e instanceof ServiceException) throw (ServiceException) e;
                    throw new ServiceException("变更后内容格式无效");
                }
            }
            if ("WBS".equals(module) && "DELETE".equals(operation))
                validateWbsItem(projectId, item, Map.of());
            if ("TASK".equals(module) && "DELETE".equals(operation))
                validateTaskItem(projectId, item, Map.of(), Map.of());
            if ("DELIVERABLE".equals(module) && "DELETE".equals(operation))
                validateDeliverableItem(projectId, item, Map.of());
            if ("TEAM".equals(module) && "DELETE".equals(operation))
                validateTeamItem(projectId, item, Map.of());
            if ("PROJECT_BUDGET".equals(module) && "DELETE".equals(operation))
                validateBudgetItem(projectId, item, Map.of());
            if ("WORK_PACKAGE_BUDGET".equals(module) && "DELETE".equals(operation))
                validateWorkPackageBudgetItem(projectId, item, Map.of());
        }
    }

    private void validateBudgetItem(
            Long projectId, ProjectPlanChangeItem item, Map<String, Object> after) {
        String operation = item.getOperationType();
        if ("PROJECT_BUDGET_HEADER".equals(item.getTargetType())) {
            if (!"UPDATE".equals(operation) || after.size() != 1)
                throw new ServiceException("项目预算头一次只能修改一个字段");
            String field = after.keySet().iterator().next();
            if (!Set.of("budgetRequired", "budgetAmount", "budgetDescription").contains(field))
                throw new ServiceException("项目预算头包含未开放字段");
            if ("budgetRequired".equals(field)
                    && !Set.of("0", "1").contains(String.valueOf(after.get(field))))
                throw new ServiceException("是否需要预算仅支持是或否");
            if ("budgetAmount".equals(field)) budgetAmount(after.get(field), "预算总额");
            return;
        }
        if (!"PROJECT_BUDGET_LINE".equals(item.getTargetType()))
            throw new ServiceException("项目预算变更对象类型无效");
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank())
                throw new ServiceException("删除分类预算不能填写变更后内容");
            requireBudgetLine(projectId, item.getTargetId());
            return;
        }
        Set<String> allowed =
                "ADD".equals(operation)
                        ? Set.of("costCategoryId", "budgetAmount", "estimationBasis")
                        : Set.of("budgetAmount", "estimationBasis");
        if (after.isEmpty() || !allowed.containsAll(after.keySet()))
            throw new ServiceException("分类预算变更包含未开放字段");
        if ("ADD".equals(operation)) {
            Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
            requireActiveBudgetCategory(categoryId);
            if (budgetMapper.countByProjectAndCategory(projectId, categoryId, null) > 0)
                throw new ServiceException("同一成本类别只能存在一条项目预算");
        } else {
            requireBudgetLine(projectId, item.getTargetId());
        }
        if (after.containsKey("budgetAmount")) budgetAmount(after.get("budgetAmount"), "分类预算金额");
        if (after.containsKey("estimationBasis")
                && (after.get("estimationBasis") == null
                        || String.valueOf(after.get("estimationBasis")).trim().isEmpty()))
            throw new ServiceException("测算依据不能为空");
    }

    private BigDecimal budgetAmount(Object value, String label) {
        try {
            BigDecimal amount = new BigDecimal(String.valueOf(value));
            if (amount.signum() <= 0 || amount.scale() > 2) throw new NumberFormatException();
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (Exception e) {
            throw new ServiceException(label + "必须大于0且最多保留两位小数");
        }
    }

    /** Returns the option only when it is an effective leaf, including its ancestors' status. */
    private CostCategory requireActiveBudgetCategory(Long categoryId) {
        return costCategoryService.options().stream()
                .filter(category -> categoryId.equals(category.getCostCategoryId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("新增分类预算只能选择有效末级成本类别"));
    }

    private void validateWorkPackageBudgetItem(
            Long projectId, ProjectPlanChangeItem item, Map<String, Object> after) {
        if (!"WORK_PACKAGE_BUDGET_LINE".equals(item.getTargetType())) {
            throw new ServiceException("工作包预算变更对象类型无效");
        }
        String operation = item.getOperationType();
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank()) {
                throw new ServiceException("删除工作包预算不能填写变更后内容");
            }
            requireWorkPackageBudgetLine(projectId, item.getTargetId());
            return;
        }
        Set<String> allowed =
                "ADD".equals(operation)
                        ? Set.of(
                                "workPackageId",
                                "workPackageName",
                                "costCategoryId",
                                "categoryPath",
                                "budgetAmount",
                                "estimationBasis")
                        : Set.of("budgetAmount", "estimationBasis");
        if (after.isEmpty() || !allowed.containsAll(after.keySet())) {
            throw new ServiceException("工作包预算变更包含未开放字段");
        }
        if ("ADD".equals(operation)) {
            Long workPackageId = numberValue(after.get("workPackageId"), "工作包");
            Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
            ProjectWbsNode workPackage = requireWorkPackage(projectId, workPackageId);
            if ("COMPLETED".equals(workPackage.getStatus())) {
                throw new ServiceException("已完成工作包不允许新增预算");
            }
            requireActiveBudgetCategory(categoryId);
            if (workPackageBudgetMapper.countByWorkPackageAndCategory(
                            workPackageId, categoryId, null)
                    > 0) {
                throw new ServiceException("同一工作包不能重复分配相同成本类别预算");
            }
        } else {
            ProjectWorkPackageBudgetLine line =
                    requireWorkPackageBudgetLine(projectId, item.getTargetId());
            if ("COMPLETED".equals(line.getWorkPackageStatus())) {
                throw new ServiceException("已完成工作包预算只读");
            }
        }
        if (after.containsKey("budgetAmount")) budgetAmount(after.get("budgetAmount"), "工作包预算金额");
        if (after.containsKey("estimationBasis")
                && (after.get("estimationBasis") == null
                        || String.valueOf(after.get("estimationBasis")).trim().isEmpty())) {
            throw new ServiceException("测算依据不能为空");
        }
    }

    @SuppressWarnings("unchecked")
    private void validateFinalBudget(Long projectId, List<ProjectPlanChangeItem> items) {
        ProjectInfo header = requireProject(projectId);
        Map<Long, ProjectBudgetLine> byLineId = new LinkedHashMap<>();
        Map<Long, ProjectBudgetLine> byCategoryId = new LinkedHashMap<>();
        for (ProjectBudgetLine line : budgetMapper.selectByProjectId(projectId)) {
            byLineId.put(line.getBudgetLineId(), line);
            byCategoryId.put(line.getCostCategoryId(), line);
        }
        for (ProjectPlanChangeItem item : items) {
            if (!"PROJECT_BUDGET".equals(item.getModuleType())) continue;
            try {
                Map<String, Object> after =
                        item.getAfterJson() == null
                                ? Map.of()
                                : objectMapper.readValue(item.getAfterJson(), Map.class);
                if ("PROJECT_BUDGET_HEADER".equals(item.getTargetType())) {
                    String field = after.keySet().iterator().next();
                    Object value = after.get(field);
                    if ("budgetRequired".equals(field)) {
                        header.setBudgetRequired(String.valueOf(value));
                        if ("0".equals(header.getBudgetRequired())) {
                            header.setBudgetAmount(null);
                            header.setBudgetDescription(null);
                        }
                    }
                    if ("budgetAmount".equals(field))
                        header.setBudgetAmount(budgetAmount(value, "预算总额"));
                    if ("budgetDescription".equals(field))
                        header.setBudgetDescription(value == null ? null : String.valueOf(value));
                    continue;
                }
                if ("ADD".equals(item.getOperationType())) {
                    Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
                    if (byCategoryId.containsKey(categoryId))
                        throw new ServiceException("分类预算成本类别重复");
                    ProjectBudgetLine line = new ProjectBudgetLine();
                    line.setCostCategoryId(categoryId);
                    line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "分类预算金额"));
                    line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
                    byCategoryId.put(categoryId, line);
                } else {
                    ProjectBudgetLine persisted = requireBudgetLine(projectId, item.getTargetId());
                    ProjectBudgetLine line = byLineId.get(persisted.getBudgetLineId());
                    if (line == null) throw new ServiceException("分类预算不存在或不属于当前项目");
                    if ("DELETE".equals(item.getOperationType())) {
                        byCategoryId.remove(line.getCostCategoryId());
                    } else {
                        BigDecimal oldAmount = line.getBudgetAmount();
                        if (after.containsKey("budgetAmount"))
                            line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "分类预算金额"));
                        if (after.containsKey("estimationBasis"))
                            line.setEstimationBasis(
                                    String.valueOf(after.get("estimationBasis")).trim());
                        if ("1".equals(line.getCategoryStatus())
                                && line.getBudgetAmount().compareTo(oldAmount) > 0)
                            throw new ServiceException("停用成本类别仅允许调减或删除");
                    }
                }
            } catch (Exception e) {
                if (e instanceof ServiceException) throw (ServiceException) e;
                throw new ServiceException("项目预算变更内容格式无效");
            }
        }
        Map<Long, BigDecimal> actualByCategory = new LinkedHashMap<>();
        for (ProjectActualCostAggregate item : actualCostMapper.categoryTotals(projectId)) {
            actualByCategory.put(item.getCostCategoryId(), item.getActualAmount());
        }
        if ("0".equals(header.getBudgetRequired())) {
            if (!actualByCategory.isEmpty()) throw new ServiceException("项目已发生实际成本，不能取消预算");
            if (!byCategoryId.isEmpty()) throw new ServiceException("取消预算时必须同时删除全部分类预算");
            return;
        }
        for (Map.Entry<Long, BigDecimal> entry : actualByCategory.entrySet()) {
            if (entry.getValue() == null || entry.getValue().signum() <= 0) continue;
            ProjectBudgetLine finalLine = byCategoryId.get(entry.getKey());
            if (finalLine == null) throw new ServiceException("成本类别已发生实际成本，不能删除项目分类预算");
            if (finalLine.getBudgetAmount().compareTo(entry.getValue()) < 0)
                throw new ServiceException("项目分类预算不能低于该类别已发生实际成本");
        }
        if (header.getBudgetAmount() == null || header.getBudgetAmount().signum() <= 0)
            throw new ServiceException("启用预算时预算总额必须大于0");
        if (byCategoryId.isEmpty()) throw new ServiceException("启用预算时至少需要一条分类预算");
        BigDecimal allocated =
                byCategoryId.values().stream()
                        .map(ProjectBudgetLine::getBudgetAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (allocated.compareTo(header.getBudgetAmount()) != 0)
            throw new ServiceException("分类预算合计必须严格等于预算总额");
        validateFinalWorkPackageBudget(projectId, items, byCategoryId);
    }

    @SuppressWarnings("unchecked")
    private void validateFinalWorkPackageBudget(
            Long projectId,
            List<ProjectPlanChangeItem> items,
            Map<Long, ProjectBudgetLine> projectBudgetByCategory) {
        Map<Long, ProjectWorkPackageBudgetLine> byLineId = new LinkedHashMap<>();
        Map<String, ProjectWorkPackageBudgetLine> byBusinessKey = new LinkedHashMap<>();
        for (ProjectWorkPackageBudgetLine line :
                workPackageBudgetMapper.selectByProjectId(projectId)) {
            byLineId.put(line.getWorkPackageBudgetLineId(), line);
            byBusinessKey.put(
                    workPackageBudgetKey(line.getWorkPackageId(), line.getCostCategoryId()), line);
        }
        for (ProjectPlanChangeItem item : items) {
            if (!"WORK_PACKAGE_BUDGET".equals(item.getModuleType())) continue;
            try {
                Map<String, Object> after =
                        item.getAfterJson() == null
                                ? Map.of()
                                : objectMapper.readValue(item.getAfterJson(), Map.class);
                if ("ADD".equals(item.getOperationType())) {
                    Long workPackageId = numberValue(after.get("workPackageId"), "工作包");
                    Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
                    String key = workPackageBudgetKey(workPackageId, categoryId);
                    if (byBusinessKey.containsKey(key)) {
                        throw new ServiceException("同一工作包不能重复分配相同成本类别预算");
                    }
                    ProjectWorkPackageBudgetLine line = new ProjectWorkPackageBudgetLine();
                    line.setProjectId(projectId);
                    line.setWorkPackageId(workPackageId);
                    line.setCostCategoryId(categoryId);
                    line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "工作包预算金额"));
                    line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
                    byBusinessKey.put(key, line);
                    continue;
                }
                ProjectWorkPackageBudgetLine persisted =
                        requireWorkPackageBudgetLine(projectId, item.getTargetId());
                ProjectWorkPackageBudgetLine line =
                        byLineId.get(persisted.getWorkPackageBudgetLineId());
                if (line == null) throw new ServiceException("工作包预算不存在或不属于当前项目");
                if ("DELETE".equals(item.getOperationType())) {
                    byBusinessKey.remove(
                            workPackageBudgetKey(
                                    line.getWorkPackageId(), line.getCostCategoryId()));
                } else {
                    BigDecimal oldAmount = line.getBudgetAmount();
                    if (after.containsKey("budgetAmount")) {
                        line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "工作包预算金额"));
                    }
                    if (after.containsKey("estimationBasis")) {
                        line.setEstimationBasis(
                                String.valueOf(after.get("estimationBasis")).trim());
                    }
                    if ("1".equals(line.getCategoryStatus())
                            && line.getBudgetAmount().compareTo(oldAmount) > 0) {
                        throw new ServiceException("停用成本类别仅允许调减或删除工作包预算");
                    }
                }
            } catch (Exception e) {
                if (e instanceof ServiceException) throw (ServiceException) e;
                throw new ServiceException("工作包预算变更内容格式无效");
            }
        }
        for (ProjectActualCostAggregate item : actualCostMapper.workPackageTotals(projectId)) {
            if (item.getActualAmount() == null || item.getActualAmount().signum() <= 0) continue;
            String key = workPackageBudgetKey(item.getWorkPackageId(), item.getCostCategoryId());
            ProjectWorkPackageBudgetLine finalLine = byBusinessKey.get(key);
            if (finalLine == null) throw new ServiceException("删除工作包预算前必须删除该工作包该类别实际成本");
            if (finalLine.getBudgetAmount().compareTo(item.getActualAmount()) < 0)
                throw new ServiceException("工作包预算不能低于该工作包已发生实际成本");
        }
        Map<Long, BigDecimal> allocatedByCategory = new LinkedHashMap<>();
        for (ProjectWorkPackageBudgetLine line : byBusinessKey.values()) {
            if (!projectBudgetByCategory.containsKey(line.getCostCategoryId())) {
                throw new ServiceException("工作包预算的成本类别未配置项目分类预算");
            }
            allocatedByCategory.merge(
                    line.getCostCategoryId(), line.getBudgetAmount(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : allocatedByCategory.entrySet()) {
            if (entry.getValue()
                            .compareTo(
                                    projectBudgetByCategory.get(entry.getKey()).getBudgetAmount())
                    > 0) {
                throw new ServiceException("工作包预算分配合计不能超过项目分类预算");
            }
        }
        for (ProjectPlanChangeItem item : items) {
            if (!"WBS".equals(item.getModuleType()) || !"DELETE".equals(item.getOperationType()))
                continue;
            boolean hasBudget =
                    byBusinessKey.values().stream()
                            .anyMatch(line -> item.getTargetId().equals(line.getWorkPackageId()));
            if (hasBudget) throw new ServiceException("删除工作包前必须先删除其全部工作包预算");
        }
    }

    private String workPackageBudgetKey(Long workPackageId, Long categoryId) {
        return workPackageId + ":" + categoryId;
    }

    private ProjectBudgetLine requireBudgetLine(Long projectId, Long lineId) {
        ProjectBudgetLine line = lineId == null ? null : budgetMapper.selectById(lineId);
        if (line == null || !projectId.equals(line.getProjectId()))
            throw new ServiceException("分类预算不存在或不属于当前项目");
        return line;
    }

    private ProjectWorkPackageBudgetLine requireWorkPackageBudgetLine(Long projectId, Long lineId) {
        ProjectWorkPackageBudgetLine line =
                lineId == null ? null : workPackageBudgetMapper.selectById(lineId);
        if (line == null || !projectId.equals(line.getProjectId())) {
            throw new ServiceException("工作包预算不存在或不属于当前项目");
        }
        return line;
    }

    private void validateTeamItem(
            Long projectId, ProjectPlanChangeItem item, Map<String, Object> after) {
        String operation = item.getOperationType();
        Set<String> addFields =
                Set.of(
                        "personId",
                        "personName",
                        "roleId",
                        "roleName",
                        "professionalRoleId",
                        "professionalRoleName",
                        "responsibility",
                        "joinDate",
                        "remark");
        Set<String> updateFields =
                Set.of(
                        "roleId",
                        "roleName",
                        "professionalRoleId",
                        "professionalRoleName",
                        "responsibility",
                        "remark");
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank())
                throw new ServiceException("成员退出不能填写变更后内容");
            validateTeamExit(requireTeamMember(projectId, item.getTargetId()));
            return;
        }
        Set<String> allowed = "ADD".equals(operation) ? addFields : updateFields;
        if (after.isEmpty() || !allowed.containsAll(after.keySet()))
            throw new ServiceException("项目团队变更包含未开放字段");
        if (after.containsKey("personName") && !after.containsKey("personId"))
            throw new ServiceException("成员显示名称不能独立变更");
        if (after.containsKey("roleName") && !after.containsKey("roleId"))
            throw new ServiceException("项目角色显示名称不能独立变更");
        if (after.containsKey("professionalRoleName") && !after.containsKey("professionalRoleId"))
            throw new ServiceException("专业角色显示名称不能独立变更");
        try {
            ProjectMember requested = objectMapper.convertValue(after, ProjectMember.class);
            requested.setProjectId(projectId);
            if ("ADD".equals(operation)) {
                validateNewTeamMember(requested);
                return;
            }
            ProjectMember old = requireTeamMember(projectId, item.getTargetId());
            if ("PROJECT_MANAGER".equals(old.getRoleCode()))
                throw new ServiceException("项目负责人请通过项目基础信息变更");
            mergeTeamMember(old, requested);
            validateTeamMemberFields(requested);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("项目团队变更字段格式无效");
        }
    }

    private void validateDeliverableItem(
            Long projectId, ProjectPlanChangeItem item, Map<String, Object> after) {
        String operation = item.getOperationType();
        Set<String> addFields =
                Set.of(
                        "workPackageId",
                        "workPackageName",
                        "deliverableName",
                        "deliverableType",
                        "requiredFlag",
                        "approvalRequired",
                        "plannedDate",
                        "acceptanceCriteria",
                        "description");
        Set<String> updateFields =
                Set.of(
                        "deliverableName",
                        "deliverableType",
                        "requiredFlag",
                        "approvalRequired",
                        "plannedDate",
                        "acceptanceCriteria",
                        "description");
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank())
                throw new ServiceException("删除交付物要求不能填写变更后内容");
            validateDeliverableDelete(requireDeliverable(projectId, item.getTargetId()));
            return;
        }
        Set<String> allowed = "ADD".equals(operation) ? addFields : updateFields;
        if (after.isEmpty() || !allowed.containsAll(after.keySet()))
            throw new ServiceException("交付物要求变更包含未开放字段");
        if (after.containsKey("workPackageName") && !after.containsKey("workPackageId"))
            throw new ServiceException("工作包显示名称不能独立变更");
        try {
            ProjectDeliverable requested =
                    objectMapper.convertValue(after, ProjectDeliverable.class);
            requested.setProjectId(projectId);
            if ("ADD".equals(operation)) {
                validateDeliverableFields(requested);
                return;
            }
            ProjectDeliverable old = requireDeliverable(projectId, item.getTargetId());
            if (deliverableMapper.countSubmissions(old.getDeliverableId()) > 0
                    && !Set.of("description").containsAll(after.keySet())) {
                throw new ServiceException("已有提交记录的交付物要求仅允许修改交付说明");
            }
            mergeDeliverable(old, requested);
            if (after.containsKey("description"))
                requested.setDescription((String) after.get("description"));
            validateDeliverableFields(requested);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("交付物要求变更字段格式无效");
        }
    }

    private void validateTaskItem(
            Long projectId,
            ProjectPlanChangeItem item,
            Map<String, Object> after,
            Map<Long, ProjectWbsNode> pendingWorkPackages) {
        String operation = item.getOperationType();
        Set<String> addFields =
                Set.of(
                        "taskType",
                        "workPackageId",
                        "workPackageName",
                        "parentTaskId",
                        "parentTaskName",
                        "taskName",
                        "description",
                        "assigneeId",
                        "assigneeName",
                        "planStartDate",
                        "planEndDate");
        Set<String> updateFields =
                Set.of(
                        "taskName",
                        "description",
                        "assigneeId",
                        "assigneeName",
                        "planStartDate",
                        "planEndDate");
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank())
                throw new ServiceException("删除任务不能填写变更后内容");
            validateTaskDelete(projectId, requireTask(projectId, item.getTargetId()));
            return;
        }
        Set<String> allowed = "ADD".equals(operation) ? addFields : updateFields;
        if (after.isEmpty() || !allowed.containsAll(after.keySet()))
            throw new ServiceException("任务变更包含未开放字段");
        if (after.containsKey("assigneeName") && !after.containsKey("assigneeId"))
            throw new ServiceException("执行人显示名称不能独立变更");
        if (after.containsKey("workPackageName") && !after.containsKey("workPackageId"))
            throw new ServiceException("工作包显示名称不能独立变更");
        if (after.containsKey("parentTaskName") && !after.containsKey("parentTaskId"))
            throw new ServiceException("上级任务显示名称不能独立变更");
        try {
            ProjectTask requested = objectMapper.convertValue(after, ProjectTask.class);
            requested.setProjectId(projectId);
            if ("ADD".equals(operation)) {
                ProjectWbsNode workPackage;
                if (requested.getWorkPackageId() != null && requested.getWorkPackageId() < 0) {
                    workPackage = pendingWorkPackages.get(requested.getWorkPackageId());
                    if (workPackage == null) throw new ServiceException("任务引用的工作包必须在同一变更单中新增");
                } else {
                    workPackage = requireWorkPackage(projectId, requested.getWorkPackageId());
                }
                validateTaskParent(projectId, requested);
                validateTaskFields(requested, workPackage);
                return;
            }
            ProjectTask old = requireTask(projectId, item.getTargetId());
            if ("COMPLETED".equals(old.getStatus())) throw new ServiceException("已完成任务不允许变更");
            if ("SUMMARY".equals(old.getTaskType())
                    && !Set.of("taskName", "description").containsAll(after.keySet()))
                throw new ServiceException("汇总任务仅允许修改名称和说明");
            mergeTask(old, requested);
            if (after.containsKey("description"))
                requested.setDescription((String) after.get("description"));
            validateTaskUpdate(old, requested, after.keySet());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("任务变更字段格式无效");
        }
    }

    private void validateWbsItem(
            Long projectId, ProjectPlanChangeItem item, Map<String, Object> after) {
        String operation = item.getOperationType();
        Set<String> addFields =
                Set.of(
                        "nodeType",
                        "parentId",
                        "parentName",
                        "wbsName",
                        "scopeDescription",
                        "ownerId",
                        "ownerName",
                        "planStartDate",
                        "planEndDate",
                        "acceptanceCriteria",
                        "definitionOfDone");
        Set<String> updateFields =
                Set.of(
                        "wbsName",
                        "scopeDescription",
                        "ownerId",
                        "ownerName",
                        "planStartDate",
                        "planEndDate",
                        "acceptanceCriteria",
                        "definitionOfDone");
        if ("DELETE".equals(operation)) {
            if (item.getAfterJson() != null && !item.getAfterJson().isBlank())
                throw new ServiceException("删除 WBS 节点不能填写变更后内容");
            validateWbsDelete(projectId, requireWbs(projectId, item.getTargetId()));
            return;
        }
        Set<String> allowed = "ADD".equals(operation) ? addFields : updateFields;
        if (after.isEmpty() || !allowed.containsAll(after.keySet()))
            throw new ServiceException("WBS 变更包含未开放字段");
        if (after.containsKey("ownerName") && !after.containsKey("ownerId"))
            throw new ServiceException("负责人显示名称不能独立变更");
        if (after.containsKey("parentName") && !after.containsKey("parentId"))
            throw new ServiceException("上级节点显示名称不能独立变更");
        try {
            ProjectWbsNode requested = objectMapper.convertValue(after, ProjectWbsNode.class);
            requested.setProjectId(projectId);
            if ("ADD".equals(operation)) {
                validateWbsParent(projectId, requested.getParentId());
                validateWbsFields(requested);
                return;
            }
            ProjectWbsNode old = requireWbs(projectId, item.getTargetId());
            if ("COMPLETED".equals(old.getStatus())) throw new ServiceException("已完成 WBS 节点不允许变更");
            if ("SUMMARY".equals(old.getNodeType())
                    && !Set.of("wbsName", "scopeDescription").containsAll(after.keySet()))
                throw new ServiceException("汇总 WBS 仅允许修改名称和范围说明");
            mergeWbs(old, requested);
            if (after.containsKey("scopeDescription"))
                requested.setScopeDescription((String) after.get("scopeDescription"));
            validateWbsUpdate(old, requested, after.keySet());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("WBS 变更字段格式无效");
        }
    }

    private void validateAttachments(List<ProjectPlanChangeAttachment> attachments) {
        if (attachments == null) return;
        for (ProjectPlanChangeAttachment attachment : attachments) {
            if (attachment == null
                    || attachment.getFileUrl() == null
                    || attachment.getFileUrl().isBlank()
                    || !attachment.getFileUrl().contains(Constants.RESOURCE_PREFIX)
                    || !FileUtils.checkAllowDownload(attachment.getFileUrl())) {
                throw new ServiceException("变更附件地址无效，仅允许下载已上传的受控文件");
            }
            if (attachment.getFileName() == null || attachment.getFileName().isBlank()) {
                throw new ServiceException("变更附件名称不能为空");
            }
            if (attachment.getFileName().length() > 255
                    || attachment.getFileUrl().length() > 1000) {
                throw new ServiceException("变更附件名称或地址过长");
            }
        }
    }

    private void applyItem(
            ProjectPlanChange change,
            ProjectPlanChangeItem item,
            String operator,
            Map<Long, Long> appliedWorkPackages) {
        if (item.getModuleType() == null || item.getOperationType() == null)
            throw new ServiceException("变更项缺少模块或操作类型");
        if (!"ADD".equals(item.getOperationType())
                && !"UPDATE".equals(item.getOperationType())
                && !"DELETE".equals(item.getOperationType()))
            throw new ServiceException("变更项操作类型无效");
        if ("PROJECT_INFO".equals(item.getModuleType())
                && "UPDATE".equals(item.getOperationType())) {
            applyProjectInfo(change, item, operator);
            return;
        }
        if ("PROJECT_BUDGET".equals(item.getModuleType())) {
            applyBudget(change, item, operator);
            return;
        }
        if ("WORK_PACKAGE_BUDGET".equals(item.getModuleType())) {
            applyWorkPackageBudget(change, item, operator);
            return;
        }
        if ("WBS".equals(item.getModuleType())) {
            applyWbs(change, item, operator, appliedWorkPackages);
            return;
        }
        if ("TASK".equals(item.getModuleType())) {
            applyTask(change, item, operator, appliedWorkPackages);
            return;
        }
        if ("DELIVERABLE".equals(item.getModuleType())) {
            applyDeliverable(change, item, operator);
            return;
        }
        if ("TEAM".equals(item.getModuleType())) {
            applyTeam(change, item, operator);
            return;
        }
        throw new ServiceException("不支持的变更模块：" + item.getModuleType());
    }

    /**
     * 按计划依赖关系应用变更项，避免同一张变更单中出现“先删父级、后删子级”或“先改任务、后改工作包周期”等中间状态。 变更清单的展示顺序仍以用户提交的 sort_order
     * 为准，这里只调整事务内的写入顺序。
     */
    private List<ProjectPlanChangeItem> orderedApplyItems(List<ProjectPlanChangeItem> items) {
        List<ProjectPlanChangeItem> ordered = new ArrayList<>(items);
        ordered.sort(
                Comparator.comparingInt(this::applyPriority)
                        .thenComparing(
                                (left, right) -> {
                                    if (!"DELETE".equals(left.getOperationType())
                                            || !"DELETE".equals(right.getOperationType())
                                            || !left.getModuleType()
                                                    .equals(right.getModuleType())) {
                                        return 0;
                                    }
                                    return Integer.compare(deleteDepth(right), deleteDepth(left));
                                })
                        .thenComparing(
                                item ->
                                        item.getSortOrder() == null
                                                ? Integer.MAX_VALUE
                                                : item.getSortOrder()));
        return ordered;
    }

    private int applyPriority(ProjectPlanChangeItem item) {
        String module = item.getModuleType();
        String operation = item.getOperationType();
        if ("TEAM".equals(module) && !"DELETE".equals(operation)) return 5;
        if ("PROJECT_INFO".equals(module)) return 10;
        if ("PROJECT_BUDGET".equals(module)) return 15;
        if ("WBS".equals(module) && !"DELETE".equals(operation)) return 20;
        if ("WORK_PACKAGE_BUDGET".equals(module) && !"DELETE".equals(operation)) return 25;
        if ("TASK".equals(module) && !"DELETE".equals(operation)) return 30;
        if ("DELIVERABLE".equals(module) && !"DELETE".equals(operation)) return 40;
        if ("DELIVERABLE".equals(module)) return 60;
        if ("TASK".equals(module)) return 70;
        if ("TEAM".equals(module)) return 80;
        if ("WORK_PACKAGE_BUDGET".equals(module)) return 85;
        if ("WBS".equals(module)) return 90;
        return 100;
    }

    private int deleteDepth(ProjectPlanChangeItem item) {
        if (item.getTargetId() == null) return 0;
        if ("WBS".equals(item.getModuleType())) {
            return wbsDepth(item.getTargetId(), new HashSet<>());
        }
        if ("TASK".equals(item.getModuleType())) {
            return taskDepth(item.getTargetId(), new HashSet<>());
        }
        return 0;
    }

    private int wbsDepth(Long id, Set<Long> visited) {
        if (id == null || !visited.add(id)) return 0;
        ProjectWbsNode node = wbsMapper.selectById(id);
        if (node == null || node.getParentId() == null || node.getParentId() == 0) return 0;
        return 1 + wbsDepth(node.getParentId(), visited);
    }

    private int taskDepth(Long id, Set<Long> visited) {
        if (id == null || !visited.add(id)) return 0;
        ProjectTask task = taskMapper.selectById(id);
        if (task == null || task.getParentTaskId() == null || task.getParentTaskId() == 0) return 0;
        return 1 + taskDepth(task.getParentTaskId(), visited);
    }

    /** 项目基础信息按字段作为最小变更单元，禁止借由 JSON 覆盖未开放字段。 */
    @SuppressWarnings("unchecked")
    private void applyProjectInfo(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            Map<String, Object> after = objectMapper.readValue(item.getAfterJson(), Map.class);
            validateProjectInfoAfter(after);
            String field = after.keySet().iterator().next();
            Object value = after.get(field);
            ProjectInfo old = requireProject(change.getProjectId());
            Long previousManagerId = old.getManagerId();
            switch (field) {
                case "projectName" -> old.setProjectName(String.valueOf(value).trim());
                case "categoryId" -> {
                    Long categoryId = numberValue(value, "项目分类");
                    if (categoryMapper.selectProjectCategoryById(categoryId) == null)
                        throw new ServiceException("项目分类不存在");
                    old.setCategoryId(categoryId);
                }
                case "managerId" -> {
                    Long managerId = numberValue(value, "项目负责人");
                    if (!teamService.isActiveMember(change.getProjectId(), managerId))
                        throw new ServiceException("项目负责人必须是当前项目活动成员");
                    old.setManagerId(managerId);
                }
                case "startDate" -> {
                    java.time.LocalDate start = java.time.LocalDate.parse(String.valueOf(value));
                    if (old.getStartDate() != null && start.isBefore(old.getStartDate()))
                        throw new ServiceException("项目计划开始日期仅允许延后");
                    old.setStartDate(start);
                }
                case "endDate" -> old.setEndDate(java.time.LocalDate.parse(String.valueOf(value)));
                case "projectBackground" -> old.setProjectBackground(String.valueOf(value));
                case "projectGoal" -> old.setProjectGoal(String.valueOf(value));
                case "projectScope" -> old.setProjectScope(String.valueOf(value));
                case "outOfScope" -> old.setOutOfScope(String.valueOf(value));
                case "expectedOutcome" -> old.setExpectedOutcome(String.valueOf(value));
                case "resourceRequirement" -> old.setResourceRequirement(String.valueOf(value));
                case "majorRisk" -> old.setMajorRisk(String.valueOf(value));
                case "technicalFeasibility" -> old.setTechnicalFeasibility(String.valueOf(value));
                case "resourceFeasibility" -> old.setResourceFeasibility(String.valueOf(value));
                case "feasibilityConclusion" -> old.setFeasibilityConclusion(String.valueOf(value));
                default -> throw new ServiceException("项目基础信息字段不支持变更");
            }
            validateProjectInfoSchedule(change.getProjectId(), old);
            if ("1".equals(old.getBudgetRequired()) && old.getBudgetAmount() == null)
                throw new ServiceException("需要预算时必须填写预算金额");
            if (old.getBudgetAmount() != null && old.getBudgetAmount().signum() < 0)
                throw new ServiceException("预算金额不能为负数");
            old.setUpdateBy(operator);
            projectMapper.updateProjectInfo(old);
            if ("managerId".equals(field) && !old.getManagerId().equals(previousManagerId))
                teamService.ensureManager(
                        change.getProjectId(), old.getManagerId(), previousManagerId, operator);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("项目基础信息变更数据无效");
        }
    }

    private void validateProjectInfoAfter(Map<String, Object> after) {
        if (after == null || after.size() != 1) throw new ServiceException("项目基础信息变更一次只能修改一个字段");
        String field = after.keySet().iterator().next();
        if (!Set.of(
                                "projectName",
                                "categoryId",
                                "managerId",
                                "startDate",
                                "endDate",
                                "projectBackground",
                                "projectGoal",
                                "projectScope",
                                "outOfScope",
                                "expectedOutcome",
                                "resourceRequirement",
                                "majorRisk",
                                "technicalFeasibility",
                                "resourceFeasibility",
                                "feasibilityConclusion")
                        .contains(field)
                || after.get(field) == null
                || String.valueOf(after.get(field)).trim().isEmpty())
            throw new ServiceException("项目基础信息变更字段或新值无效");
    }

    @SuppressWarnings("unchecked")
    private void applyBudget(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            Map<String, Object> after =
                    item.getAfterJson() == null
                            ? Map.of()
                            : objectMapper.readValue(item.getAfterJson(), Map.class);
            validateBudgetItem(change.getProjectId(), item, after);
            if ("PROJECT_BUDGET_HEADER".equals(item.getTargetType())) {
                ProjectInfo project = requireProject(change.getProjectId());
                String field = after.keySet().iterator().next();
                Object value = after.get(field);
                if ("budgetRequired".equals(field)) {
                    project.setBudgetRequired(String.valueOf(value));
                    if ("0".equals(project.getBudgetRequired())) {
                        project.setBudgetAmount(null);
                        project.setBudgetDescription(null);
                    }
                }
                if ("budgetAmount".equals(field))
                    project.setBudgetAmount(budgetAmount(value, "预算总额"));
                if ("budgetDescription".equals(field))
                    project.setBudgetDescription(value == null ? null : String.valueOf(value));
                project.setUpdateBy(operator);
                projectMapper.updateBudgetHeader(project);
                return;
            }
            if ("ADD".equals(item.getOperationType())) {
                Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
                CostCategory category = requireActiveBudgetCategory(categoryId);
                ProjectBudgetLine line = new ProjectBudgetLine();
                line.setProjectId(change.getProjectId());
                line.setCostCategoryId(categoryId);
                line.setCategoryCode(category.getCategoryCode());
                line.setCategoryName(category.getCategoryName());
                line.setCategoryPath(
                        category.getFullPath() == null
                                ? category.getCategoryName()
                                : category.getFullPath());
                line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "分类预算金额"));
                line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
                line.setSortOrder(item.getSortOrder());
                line.setCreateBy(operator);
                budgetMapper.insert(line);
                return;
            }
            ProjectBudgetLine line = requireBudgetLine(change.getProjectId(), item.getTargetId());
            if ("DELETE".equals(item.getOperationType())) {
                budgetMapper.deleteById(line.getBudgetLineId());
                return;
            }
            if (after.containsKey("budgetAmount"))
                line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "分类预算金额"));
            if (after.containsKey("estimationBasis"))
                line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
            line.setUpdateBy(operator);
            budgetMapper.update(line);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("项目预算变更数据无效");
        }
    }

    @SuppressWarnings("unchecked")
    private void applyWorkPackageBudget(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            Map<String, Object> after =
                    item.getAfterJson() == null
                            ? Map.of()
                            : objectMapper.readValue(item.getAfterJson(), Map.class);
            validateWorkPackageBudgetItem(change.getProjectId(), item, after);
            if ("ADD".equals(item.getOperationType())) {
                Long workPackageId = numberValue(after.get("workPackageId"), "工作包");
                Long categoryId = numberValue(after.get("costCategoryId"), "成本类别");
                CostCategory category = requireActiveBudgetCategory(categoryId);
                ProjectWorkPackageBudgetLine line = new ProjectWorkPackageBudgetLine();
                line.setProjectId(change.getProjectId());
                line.setWorkPackageId(workPackageId);
                line.setCostCategoryId(categoryId);
                line.setCategoryCode(category.getCategoryCode());
                line.setCategoryName(category.getCategoryName());
                line.setCategoryPath(
                        category.getFullPath() == null
                                ? category.getCategoryName()
                                : category.getFullPath());
                line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "工作包预算金额"));
                line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
                line.setSortOrder(item.getSortOrder());
                line.setCreateBy(operator);
                workPackageBudgetMapper.insert(line);
                return;
            }
            ProjectWorkPackageBudgetLine line =
                    requireWorkPackageBudgetLine(change.getProjectId(), item.getTargetId());
            if ("DELETE".equals(item.getOperationType())) {
                workPackageBudgetMapper.deleteById(line.getWorkPackageBudgetLineId());
                return;
            }
            if (after.containsKey("budgetAmount")) {
                line.setBudgetAmount(budgetAmount(after.get("budgetAmount"), "工作包预算金额"));
            }
            if (after.containsKey("estimationBasis")) {
                line.setEstimationBasis(String.valueOf(after.get("estimationBasis")).trim());
            }
            line.setUpdateBy(operator);
            workPackageBudgetMapper.update(line);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("工作包预算变更数据无效");
        }
    }

    private Long numberValue(Object value, String label) {
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException(label + "格式无效");
        }
    }

    private void validateProjectInfoSchedule(Long projectId, ProjectInfo project) {
        if (project.getStartDate() == null
                || project.getEndDate() == null
                || project.getEndDate().isBefore(project.getStartDate()))
            throw new ServiceException("项目计划结束日期不能早于开始日期");
        ProjectWbsNode wbs = new ProjectWbsNode();
        wbs.setProjectId(projectId);
        for (ProjectWbsNode node : wbsMapper.selectList(wbs)) {
            if ((node.getPlanStartDate() != null
                            && node.getPlanStartDate().isBefore(project.getStartDate()))
                    || (node.getPlanEndDate() != null
                            && node.getPlanEndDate().isAfter(project.getEndDate())))
                throw new ServiceException("项目周期必须覆盖现有 WBS / 工作包计划日期");
        }
        ProjectTask task = new ProjectTask();
        task.setProjectId(projectId);
        for (ProjectTask node : taskMapper.selectList(task)) {
            if ((node.getPlanStartDate() != null
                            && node.getPlanStartDate().isBefore(project.getStartDate()))
                    || (node.getPlanEndDate() != null
                            && node.getPlanEndDate().isAfter(project.getEndDate())))
                throw new ServiceException("项目周期必须覆盖现有任务计划日期");
        }
    }

    private void applyWbs(
            ProjectPlanChange change,
            ProjectPlanChangeItem item,
            String operator,
            Map<Long, Long> appliedWorkPackages) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectWbsNode old = requireWbs(change.getProjectId(), item.getTargetId());
                validateWbsDelete(change.getProjectId(), old);
                if (workPackageBudgetMapper.countByWorkPackageId(old.getWbsId()) > 0) {
                    throw new ServiceException("删除工作包前必须先删除其全部工作包预算");
                }
                wbsMapper.deleteById(old.getWbsId());
                return;
            }
            ProjectWbsNode requested =
                    objectMapper.readValue(item.getAfterJson(), ProjectWbsNode.class);
            requested.setProjectId(change.getProjectId());
            requested.setUpdateBy(operator);
            if ("ADD".equals(item.getOperationType())) {
                Long parent = requested.getParentId() == null ? 0L : requested.getParentId();
                ProjectWbsNode parentNode = validateWbsParent(change.getProjectId(), parent);
                validateWbsFields(requested);
                List<ProjectWbsNode> siblings =
                        wbsMapper.selectChildren(change.getProjectId(), parent);
                String prefix = parent == 0 ? "" : parentNode.getWbsCode() + ".";
                int codeNo = 1;
                String code;
                do {
                    code = prefix + codeNo++;
                } while (hasWbsCode(siblings, code));
                requested.setParentId(parent);
                requested.setWbsCode(code);
                requested.setCreateBy(operator);
                if (requested.getStatus() == null) requested.setStatus("NOT_STARTED");
                if (requested.getProgress() == null) requested.setProgress(0);
                if (requested.getDeliverableRequired() == null)
                    requested.setDeliverableRequired("0");
                if (requested.getSortOrder() == null) requested.setSortOrder(siblings.size());
                wbsMapper.insert(requested);
                if (item.getTargetId() != null && item.getTargetId() < 0)
                    appliedWorkPackages.put(item.getTargetId(), requested.getWbsId());
                return;
            }
            ProjectWbsNode old = requireWbs(change.getProjectId(), item.getTargetId());
            mergeWbs(old, requested);
            Map<String, Object> after = objectMapper.readValue(item.getAfterJson(), Map.class);
            if (after.containsKey("scopeDescription"))
                requested.setScopeDescription((String) after.get("scopeDescription"));
            validateWbsUpdate(old, requested, after.keySet());
            wbsMapper.update(requested);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("WBS变更数据无效");
        }
    }

    private void validateWbsFields(ProjectWbsNode node) {
        if (node.getWbsName() == null
                || node.getWbsName().isBlank()
                || !("SUMMARY".equals(node.getNodeType())
                        || "WORK_PACKAGE".equals(node.getNodeType()))) {
            throw new ServiceException("WBS必须填写有效名称和节点类型");
        }
        if ("WORK_PACKAGE".equals(node.getNodeType())) {
            if (node.getOwnerId() == null
                    || !teamService.isActiveMember(node.getProjectId(), node.getOwnerId())) {
                throw new ServiceException("工作包负责人必须是当前项目活动成员");
            }
            if (node.getPlanStartDate() == null
                    || node.getPlanEndDate() == null
                    || node.getPlanEndDate().isBefore(node.getPlanStartDate())) {
                throw new ServiceException("工作包计划日期不能为空且结束日期不能早于开始日期");
            }
            ProjectInfo project = requireProject(node.getProjectId());
            if (project.getStartDate() != null && project.getEndDate() != null) {
                if (node.getPlanStartDate().isBefore(project.getStartDate())
                        || node.getPlanEndDate().isAfter(project.getEndDate())) {
                    throw new ServiceException("工作包计划日期必须在项目周期内");
                }
            }
            if (node.getAcceptanceCriteria() == null
                    || node.getAcceptanceCriteria().isBlank()
                    || node.getDefinitionOfDone() == null
                    || node.getDefinitionOfDone().isBlank()) {
                throw new ServiceException("工作包必须填写验收标准和完成定义");
            }
        }
    }

    private ProjectWbsNode requireWbs(Long projectId, Long wbsId) {
        ProjectWbsNode node = wbsMapper.selectById(wbsId);
        if (node == null || !projectId.equals(node.getProjectId()))
            throw new ServiceException("WBS 节点不存在或不属于当前项目");
        return node;
    }

    private ProjectWbsNode validateWbsParent(Long projectId, Long parentId) {
        if (parentId == null || parentId == 0) return null;
        ProjectWbsNode parent = requireWbs(projectId, parentId);
        if (!"SUMMARY".equals(parent.getNodeType())) throw new ServiceException("工作包不能拥有下级 WBS 节点");
        return parent;
    }

    private void validateWbsDelete(Long projectId, ProjectWbsNode node) {
        if (!"NOT_STARTED".equals(node.getStatus())) throw new ServiceException("仅未开始的 WBS 节点允许删除");
        if (wbsMapper.countChildren(node.getWbsId()) > 0
                || wbsMapper.countTasks(node.getWbsId()) > 0
                || wbsMapper.countDeliverables(node.getWbsId()) > 0
                || issueMapper.countByWorkPackage(projectId, node.getWbsId()) > 0)
            throw new ServiceException("WBS 节点已有下级、任务、交付物或问题，不能删除");
    }

    private void validateWbsUpdate(
            ProjectWbsNode old, ProjectWbsNode requested, Set<String> changedFields) {
        if ("COMPLETED".equals(old.getStatus())) throw new ServiceException("已完成 WBS 节点不允许变更");
        if (changedFields.contains("planStartDate")) {
            if (!"NOT_STARTED".equals(old.getStatus()))
                throw new ServiceException("已开始的工作包不允许修改计划开始日期");
            if (old.getPlanStartDate() != null
                    && requested.getPlanStartDate().isBefore(old.getPlanStartDate()))
                throw new ServiceException("工作包计划开始日期仅允许延后");
        }
        validateWbsFields(requested);
        if ("WORK_PACKAGE".equals(requested.getNodeType())) validateWbsDateCoverage(requested);
    }

    private void validateWbsDateCoverage(ProjectWbsNode node) {
        ProjectTask taskFilter = new ProjectTask();
        taskFilter.setProjectId(node.getProjectId());
        taskFilter.setWorkPackageId(node.getWbsId());
        for (ProjectTask task : taskMapper.selectList(taskFilter)) {
            if (before(task.getPlanStartDate(), node.getPlanStartDate())
                    || after(task.getPlanEndDate(), node.getPlanEndDate())
                    || before(task.getActualStartDate(), node.getPlanStartDate())
                    || after(task.getActualEndDate(), node.getPlanEndDate()))
                throw new ServiceException("工作包周期必须覆盖所属任务的计划日期和实际日期");
        }
        ProjectDeliverable deliverableFilter = new ProjectDeliverable();
        deliverableFilter.setProjectId(node.getProjectId());
        deliverableFilter.setWorkPackageId(node.getWbsId());
        for (ProjectDeliverable deliverable : deliverableMapper.selectList(deliverableFilter)) {
            if (before(deliverable.getPlannedDate(), node.getPlanStartDate())
                    || after(deliverable.getPlannedDate(), node.getPlanEndDate()))
                throw new ServiceException("工作包周期必须覆盖所属交付物的计划日期");
        }
    }

    private boolean before(java.time.LocalDate value, java.time.LocalDate boundary) {
        return value != null && boundary != null && value.isBefore(boundary);
    }

    private boolean after(java.time.LocalDate value, java.time.LocalDate boundary) {
        return value != null && boundary != null && value.isAfter(boundary);
    }

    private void mergeWbs(ProjectWbsNode old, ProjectWbsNode requested) {
        requested.setWbsId(old.getWbsId());
        requested.setParentId(old.getParentId());
        requested.setWbsCode(old.getWbsCode());
        if (requested.getNodeType() == null) requested.setNodeType(old.getNodeType());
        if (requested.getWbsName() == null) requested.setWbsName(old.getWbsName());
        if (requested.getScopeDescription() == null)
            requested.setScopeDescription(old.getScopeDescription());
        if (requested.getOwnerId() == null && "WORK_PACKAGE".equals(old.getNodeType()))
            requested.setOwnerId(old.getOwnerId());
        if (requested.getPlanStartDate() == null)
            requested.setPlanStartDate(old.getPlanStartDate());
        if (requested.getPlanEndDate() == null) requested.setPlanEndDate(old.getPlanEndDate());
        if (requested.getAcceptanceCriteria() == null)
            requested.setAcceptanceCriteria(old.getAcceptanceCriteria());
        if (requested.getDefinitionOfDone() == null)
            requested.setDefinitionOfDone(old.getDefinitionOfDone());
        if (requested.getDeliverableRequired() == null)
            requested.setDeliverableRequired(old.getDeliverableRequired());
        if (requested.getPriority() == null) requested.setPriority(old.getPriority());
        if (requested.getEstimatedHours() == null)
            requested.setEstimatedHours(old.getEstimatedHours());
        if (requested.getBudgetAmount() == null) requested.setBudgetAmount(old.getBudgetAmount());
        requested.setStatus(old.getStatus());
        requested.setProgress(old.getProgress());
        requested.setSortOrder(old.getSortOrder());
    }

    private void applyTask(
            ProjectPlanChange change,
            ProjectPlanChangeItem item,
            String operator,
            Map<Long, Long> appliedWorkPackages) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectTask old = requireTask(change.getProjectId(), item.getTargetId());
                validateTaskDelete(change.getProjectId(), old);
                taskMapper.delete(old.getTaskId());
                return;
            }
            ProjectTask requested = objectMapper.readValue(item.getAfterJson(), ProjectTask.class);
            requested.setProjectId(change.getProjectId());
            requested.setUpdateBy(operator);
            if ("ADD".equals(item.getOperationType())) {
                Long parent =
                        requested.getParentTaskId() == null ? 0L : requested.getParentTaskId();
                if (requested.getWorkPackageId() != null && requested.getWorkPackageId() < 0) {
                    Long resolved = appliedWorkPackages.get(requested.getWorkPackageId());
                    if (resolved == null) throw new ServiceException("任务引用的新增工作包尚未应用");
                    requested.setWorkPackageId(resolved);
                }
                ProjectWbsNode wp =
                        requireWorkPackage(change.getProjectId(), requested.getWorkPackageId());
                if (requested.getTaskName() == null
                        || requested.getTaskName().isBlank()
                        || !("SUMMARY".equals(requested.getTaskType())
                                || "EXECUTION".equals(requested.getTaskType()))) {
                    throw new ServiceException("新增任务必须填写名称和任务类型");
                }
                ProjectTask parentTask = validateTaskParent(change.getProjectId(), requested);
                validateTaskFields(requested, wp);
                List<ProjectTask> siblings =
                        taskMapper.selectChildren(requested.getWorkPackageId(), parent);
                String prefix =
                        parent == 0 ? wp.getWbsCode() + "-T" : parentTask.getTaskCode() + ".";
                int codeNo = 1;
                String code;
                do {
                    code = prefix + codeNo++;
                } while (hasTaskCode(siblings, code));
                requested.setParentTaskId(parent);
                requested.setTaskCode(code);
                requested.setCreateBy(operator);
                if (requested.getStatus() == null) requested.setStatus("NOT_STARTED");
                if (requested.getProgress() == null) requested.setProgress(0);
                if (requested.getSortOrder() == null) requested.setSortOrder(siblings.size());
                taskMapper.insert(requested);
                return;
            }
            ProjectTask old = requireTask(change.getProjectId(), item.getTargetId());
            mergeTask(old, requested);
            Map<String, Object> after = objectMapper.readValue(item.getAfterJson(), Map.class);
            if (after.containsKey("description"))
                requested.setDescription((String) after.get("description"));
            validateTaskUpdate(old, requested, after.keySet());
            taskMapper.update(requested);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("任务变更数据无效");
        }
    }

    private void validateTaskFields(ProjectTask task, ProjectWbsNode wp) {
        if (task.getTaskName() == null
                || task.getTaskName().isBlank()
                || !("SUMMARY".equals(task.getTaskType())
                        || "EXECUTION".equals(task.getTaskType())))
            throw new ServiceException("任务必须填写有效名称和任务类型");
        if ("EXECUTION".equals(task.getTaskType())) {
            if (task.getAssigneeId() == null
                    || !teamService.isActiveMember(task.getProjectId(), task.getAssigneeId())) {
                throw new ServiceException("执行任务负责人必须是当前项目活动成员");
            }
            if (task.getPlanStartDate() == null
                    || task.getPlanEndDate() == null
                    || task.getPlanEndDate().isBefore(task.getPlanStartDate())) {
                throw new ServiceException("执行任务计划日期不能为空且结束日期不能早于开始日期");
            }
            if (wp != null
                    && wp.getPlanStartDate() != null
                    && wp.getPlanEndDate() != null
                    && (task.getPlanStartDate().isBefore(wp.getPlanStartDate())
                            || task.getPlanEndDate().isAfter(wp.getPlanEndDate()))) {
                throw new ServiceException("任务计划日期必须在工作包周期内");
            }
        }
    }

    private ProjectTask requireTask(Long projectId, Long taskId) {
        ProjectTask task = taskMapper.selectById(taskId);
        if (task == null || !projectId.equals(task.getProjectId()))
            throw new ServiceException("任务不存在或不属于当前项目");
        return task;
    }

    private ProjectWbsNode requireWorkPackage(Long projectId, Long workPackageId) {
        ProjectWbsNode workPackage = requireWbs(projectId, workPackageId);
        if (!"WORK_PACKAGE".equals(workPackage.getNodeType()))
            throw new ServiceException("任务必须归属工作包");
        return workPackage;
    }

    private ProjectTask validateTaskParent(Long projectId, ProjectTask task) {
        Long parentId = task.getParentTaskId() == null ? 0L : task.getParentTaskId();
        if (parentId == 0) return null;
        ProjectTask parent = requireTask(projectId, parentId);
        if (!task.getWorkPackageId().equals(parent.getWorkPackageId()))
            throw new ServiceException("上级任务必须属于同一工作包");
        if (!"SUMMARY".equals(parent.getTaskType())) throw new ServiceException("执行任务不能拥有下级任务");
        return parent;
    }

    private void validateTaskUpdate(
            ProjectTask old, ProjectTask requested, Set<String> changedFields) {
        if ("COMPLETED".equals(old.getStatus())) throw new ServiceException("已完成任务不允许变更");
        if (changedFields.contains("planStartDate")) {
            if (!"NOT_STARTED".equals(old.getStatus()))
                throw new ServiceException("已开始任务不允许修改计划开始日期");
            if (old.getPlanStartDate() != null
                    && requested.getPlanStartDate().isBefore(old.getPlanStartDate()))
                throw new ServiceException("任务计划开始日期仅允许延后");
        }
        validateTaskFields(
                requested, requireWorkPackage(old.getProjectId(), old.getWorkPackageId()));
    }

    private void validateTaskDelete(Long projectId, ProjectTask task) {
        if (!"NOT_STARTED".equals(task.getStatus())) throw new ServiceException("仅未开始任务允许删除");
        if (taskMapper.countChildren(task.getTaskId()) > 0
                || task.getActualStartDate() != null
                || task.getActualEndDate() != null
                || task.getActualHours() != null
                || !taskMapper.selectOutputs(task.getTaskId()).isEmpty()
                || issueMapper.countByTask(projectId, task.getTaskId()) > 0)
            throw new ServiceException("任务已有下级、成果、问题或执行记录，不能删除");
    }

    private void mergeTask(ProjectTask old, ProjectTask requested) {
        requested.setTaskId(old.getTaskId());
        requested.setParentTaskId(old.getParentTaskId());
        requested.setWorkPackageId(old.getWorkPackageId());
        requested.setTaskCode(old.getTaskCode());
        requested.setTaskType(old.getTaskType());
        if (requested.getTaskName() == null) requested.setTaskName(old.getTaskName());
        if (requested.getDescription() == null) requested.setDescription(old.getDescription());
        if (requested.getAssigneeId() == null) requested.setAssigneeId(old.getAssigneeId());
        if (requested.getPlanStartDate() == null)
            requested.setPlanStartDate(old.getPlanStartDate());
        if (requested.getPlanEndDate() == null) requested.setPlanEndDate(old.getPlanEndDate());
        if (requested.getEstimatedHours() == null)
            requested.setEstimatedHours(old.getEstimatedHours());
        if (requested.getActualHours() == null) requested.setActualHours(old.getActualHours());
        if (requested.getPriority() == null) requested.setPriority(old.getPriority());
        requested.setStatus(old.getStatus());
        requested.setProgress(old.getProgress());
        requested.setSortOrder(old.getSortOrder());
    }

    private void applyDeliverable(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectDeliverable old =
                        requireDeliverable(change.getProjectId(), item.getTargetId());
                validateDeliverableDelete(old);
                deliverableMapper.deleteByIds(new Long[] {old.getDeliverableId()});
                return;
            }
            ProjectDeliverable requested =
                    objectMapper.readValue(item.getAfterJson(), ProjectDeliverable.class);
            requested.setProjectId(change.getProjectId());
            requested.setUpdateBy(operator);
            if ("ADD".equals(item.getOperationType())) {
                validateDeliverableFields(requested);
                requested.setCreateBy(operator);
                requested.setStatus("PENDING");
                deliverableMapper.insert(requested);
                return;
            }
            ProjectDeliverable old = requireDeliverable(change.getProjectId(), item.getTargetId());
            Map<String, Object> after = objectMapper.readValue(item.getAfterJson(), Map.class);
            if (deliverableMapper.countSubmissions(old.getDeliverableId()) > 0
                    && !Set.of("description").containsAll(after.keySet())) {
                throw new ServiceException("已有提交记录的交付物要求仅允许修改交付说明");
            }
            mergeDeliverable(old, requested);
            if (after.containsKey("description"))
                requested.setDescription((String) after.get("description"));
            validateDeliverableFields(requested);
            deliverableMapper.update(requested);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("交付物变更数据无效");
        }
    }

    private ProjectDeliverable requireDeliverable(Long projectId, Long deliverableId) {
        ProjectDeliverable deliverable = deliverableMapper.selectById(deliverableId);
        if (deliverable == null || !projectId.equals(deliverable.getProjectId()))
            throw new ServiceException("交付物要求不存在或不属于当前项目");
        return deliverable;
    }

    private void validateDeliverableDelete(ProjectDeliverable deliverable) {
        if (deliverableMapper.countSubmissions(deliverable.getDeliverableId()) > 0
                || !"PENDING".equals(deliverable.getStatus())) {
            throw new ServiceException("仅待提交且没有提交记录的交付物要求允许删除");
        }
    }

    private void validateDeliverableFields(ProjectDeliverable deliverable) {
        ProjectWbsNode workPackage =
                requireWorkPackage(deliverable.getProjectId(), deliverable.getWorkPackageId());
        if (deliverable.getDeliverableName() == null
                || deliverable.getDeliverableName().isBlank()) {
            throw new ServiceException("交付物要求必须填写名称");
        }
        if (deliverable.getPlannedDate() == null) {
            throw new ServiceException("交付物要求必须填写计划交付日期");
        }
        if (before(deliverable.getPlannedDate(), workPackage.getPlanStartDate())
                || after(deliverable.getPlannedDate(), workPackage.getPlanEndDate())) {
            throw new ServiceException("计划交付日期必须在所属工作包周期内");
        }
        if (!List.of("0", "1").contains(deliverable.getRequiredFlag())
                || !List.of("0", "1").contains(deliverable.getApprovalRequired())) {
            throw new ServiceException("交付规则取值无效");
        }
        applyDeliverableType(deliverable);
    }

    private void mergeDeliverable(ProjectDeliverable old, ProjectDeliverable requested) {
        requested.setDeliverableId(old.getDeliverableId());
        requested.setProjectId(old.getProjectId());
        requested.setWorkPackageId(old.getWorkPackageId());
        if (requested.getDeliverableName() == null)
            requested.setDeliverableName(old.getDeliverableName());
        if (requested.getDeliverableType() == null) {
            requested.setDeliverableType(old.getDeliverableType());
            requested.setDeliverableTypeId(old.getDeliverableTypeId());
        }
        if (requested.getDescription() == null) requested.setDescription(old.getDescription());
        if (requested.getPlannedDate() == null) requested.setPlannedDate(old.getPlannedDate());
        if (requested.getAcceptanceCriteria() == null)
            requested.setAcceptanceCriteria(old.getAcceptanceCriteria());
        if (requested.getRequiredFlag() == null) requested.setRequiredFlag(old.getRequiredFlag());
        if (requested.getApprovalRequired() == null)
            requested.setApprovalRequired(old.getApprovalRequired());
        requested.setStatus(old.getStatus());
    }

    private void applyDeliverableType(ProjectDeliverable requested) {
        ProjectDeliverableType type =
                requested.getDeliverableTypeId() != null
                        ? deliverableTypeMapper.selectById(requested.getDeliverableTypeId())
                        : deliverableTypeMapper.selectByCode(requested.getDeliverableType());
        if (type == null || !"0".equals(type.getStatus()))
            throw new ServiceException("交付物类型不存在或已停用");
        requested.setDeliverableTypeId(type.getTypeId());
        requested.setDeliverableType(type.getTypeCode());
        requested.setSubmissionMode(type.getSubmissionMode());
        if (requested.getApprovalRequired() == null)
            requested.setApprovalRequired(type.getDefaultApprovalRequired());
        if (requested.getAllowedExtensions() == null)
            requested.setAllowedExtensions(
                    String.join(
                            ",",
                            type.getAllowedExtensions() == null
                                    ? List.of()
                                    : type.getAllowedExtensions()));
    }

    private void applyTeam(ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectMember old = requireTeamMember(change.getProjectId(), item.getTargetId());
                validateTeamExit(old);
                old.setUpdateBy(operator);
                old.setExitDate(java.time.LocalDate.now());
                teamMapper.exitMember(old);
                return;
            }
            ProjectMember n = objectMapper.readValue(item.getAfterJson(), ProjectMember.class);
            n.setProjectId(change.getProjectId());
            if ("ADD".equals(item.getOperationType())) {
                validateNewTeamMember(n);
                n.setStatus("ACTIVE");
                n.setCreateBy(operator);
                if (n.getJoinDate() == null) n.setJoinDate(java.time.LocalDate.now());
                teamMapper.insertMember(n);
            } else {
                ProjectMember old = requireTeamMember(change.getProjectId(), item.getTargetId());
                if ("PROJECT_MANAGER".equals(old.getRoleCode()))
                    throw new ServiceException("项目负责人请通过项目基础信息变更");
                mergeTeamMember(old, n);
                validateTeamMemberFields(n);
                n.setUpdateBy(operator);
                teamMapper.updateMember(n);
            }
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("项目团队变更数据无效");
        }
    }

    private ProjectMember requireTeamMember(Long projectId, Long memberId) {
        ProjectMember member = teamMapper.selectMemberById(memberId);
        if (member == null || !projectId.equals(member.getProjectId()))
            throw new ServiceException("项目成员不存在或不属于当前项目");
        if (!"ACTIVE".equals(member.getStatus())) throw new ServiceException("已退出成员不允许再次变更");
        return member;
    }

    private void validateNewTeamMember(ProjectMember member) {
        if (member.getPersonId() == null
                || personMapper.selectProjectPersonById(member.getPersonId()) == null) {
            throw new ServiceException("新增项目成员必须选择有效用户");
        }
        if (teamMapper.selectActiveMember(member.getProjectId(), member.getPersonId()) != null)
            throw new ServiceException("该用户已经是项目活动成员");
        validateTeamMemberFields(member);
    }

    private void validateTeamMemberFields(ProjectMember member) {
        if (member.getRoleId() == null || !validRole(member.getProjectId(), member.getRoleId()))
            throw new ServiceException("项目角色不存在或不属于当前项目");
        ProjectRole projectRole = teamMapper.selectRoleById(member.getRoleId());
        if ("PROJECT_MANAGER".equals(projectRole.getRoleCode()))
            throw new ServiceException("项目负责人请通过项目基础信息变更");
        if (member.getProfessionalRoleId() == null) throw new ServiceException("请选择专业角色");
        ProfessionalRole role = professionalRoleMapper.selectById(member.getProfessionalRoleId());
        if (role == null || !"0".equals(role.getStatus()))
            throw new ServiceException("专业角色不存在或已停用");
        member.setProfessionalRoleCode(role.getRoleCode());
        member.setProfessionalRoleName(role.getRoleName());
        member.setSpecialtyRole(role.getRoleName());
    }

    private void validateTeamExit(ProjectMember member) {
        if ("PROJECT_MANAGER".equals(member.getRoleCode()))
            throw new ServiceException("项目负责人不能退出团队，请先变更项目负责人");
        int count = teamMapper.countIncompleteTasks(member.getProjectId(), member.getPersonId());
        if (count > 0) throw new ServiceException("该成员仍负责" + count + "项未完成任务，请先转交任务");
    }

    private void mergeTeamMember(ProjectMember old, ProjectMember requested) {
        requested.setMemberId(old.getMemberId());
        requested.setProjectId(old.getProjectId());
        requested.setPersonId(old.getPersonId());
        if (requested.getRoleId() == null) requested.setRoleId(old.getRoleId());
        if (requested.getProfessionalRoleId() == null)
            requested.setProfessionalRoleId(old.getProfessionalRoleId());
        if (requested.getResponsibility() == null)
            requested.setResponsibility(old.getResponsibility());
        if (requested.getRemark() == null) requested.setRemark(old.getRemark());
        requested.setStatus(old.getStatus());
    }

    private void createNextBaseline(
            Long projectId, Long sourceChangeId, String operator, int version) {
        try {
            ProjectPlanBaseline b = new ProjectPlanBaseline();
            b.setProjectId(projectId);
            b.setVersionNo(version);
            b.setSourceChangeId(sourceChangeId);
            b.setCreateBy(operator);
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("project", requireProject(projectId));
            ProjectWbsNode f = new ProjectWbsNode();
            f.setProjectId(projectId);
            s.put("wbs", wbsMapper.selectList(f));
            ProjectTask t = new ProjectTask();
            t.setProjectId(projectId);
            s.put("tasks", taskMapper.selectList(t));
            ProjectDeliverable d = new ProjectDeliverable();
            d.setProjectId(projectId);
            s.put("deliverables", deliverableMapper.selectList(d));
            ProjectMember m = new ProjectMember();
            m.setProjectId(projectId);
            m.setStatus("ACTIVE");
            s.put("team", teamService.members(m));
            s.put("projectBudget", budgetMapper.selectByProjectId(projectId));
            s.put("workPackageBudget", workPackageBudgetMapper.selectByProjectId(projectId));
            b.setSnapshotJson(objectMapper.writeValueAsString(s));
            mapper.insertBaseline(b);
        } catch (Exception e) {
            throw new ServiceException("创建新计划基线失败");
        }
    }

    private boolean validRole(Long projectId, Long roleId) {
        com.ruoyi.projectmanagement.team.domain.ProjectRole role =
                teamMapper.selectRoleById(roleId);
        return role != null
                && (role.getProjectId() == null
                        || role.getProjectId() == 0L
                        || projectId.equals(role.getProjectId()))
                && !"1".equals(role.getStatus());
    }

    private boolean hasWbsCode(List<ProjectWbsNode> siblings, String code) {
        for (ProjectWbsNode sibling : siblings) {
            if (code.equals(sibling.getWbsCode())) return true;
        }
        return false;
    }

    private boolean hasTaskCode(List<ProjectTask> siblings, String code) {
        for (ProjectTask sibling : siblings) {
            if (code.equals(sibling.getTaskCode())) return true;
        }
        return false;
    }

    private void refreshAggregates(Long projectId) {
        ProjectWbsNode filter = new ProjectWbsNode();
        filter.setProjectId(projectId);
        filter.setNodeType("WORK_PACKAGE");
        for (ProjectWbsNode node : wbsMapper.selectList(filter)) {
            taskService.refreshPackage(node.getWbsId());
        }
        wbsService.refreshProject(projectId);
    }
}
