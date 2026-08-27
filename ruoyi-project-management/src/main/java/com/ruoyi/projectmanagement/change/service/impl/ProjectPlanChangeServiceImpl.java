package com.ruoyi.projectmanagement.change.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.projectmanagement.change.domain.*;
import com.ruoyi.projectmanagement.change.mapper.ProjectPlanChangeMapper;
import com.ruoyi.projectmanagement.change.service.IProjectPlanChangeService;
import com.ruoyi.projectmanagement.change.service.ProjectPlanChangeAuditService;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableTypeMapper;
import com.ruoyi.projectmanagement.issue.mapper.ProjectIssueMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.mapper.ProjectTeamMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import com.ruoyi.projectmanagement.workflow.service.WorkflowBusinessCallback;
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
            IProjectWbsService wbsService) {
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
            modules.put("PROJECT", compareSingle(left.get("project"), right.get("project")));
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
        if (before != null && after != null && !before.equals(after)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("before", before);
            row.put("after", after);
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
                    if (!left.containsKey(id)) added.add(value);
                    else if (!value.equals(left.get(id))) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("name", value.get(nameKey));
                        row.put("before", left.get(id));
                        row.put("after", value);
                        modified.add(row);
                    }
                });
        left.forEach(
                (id, value) -> {
                    if (!right.containsKey(id)) removed.add(value);
                });
        result.put("added", added);
        result.put("removed", removed);
        result.put("modified", modified);
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
        validateItems(change.getItems());
        validateAttachments(change.getAttachments());
        for (ProjectPlanChangeItem i :
                change.getItems() == null ? List.<ProjectPlanChangeItem>of() : change.getItems()) {
            i.setChangeId(change.getChangeId());
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
        validateItems(items);
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
            validateItems(items);
            for (ProjectPlanChangeItem item : orderedApplyItems(items)) applyItem(c, item, op);
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

    private void validateItems(List<ProjectPlanChangeItem> items) {
        if (items == null || items.isEmpty()) throw new ServiceException("请至少添加一条变更项");
        for (ProjectPlanChangeItem item : items) {
            if (item == null || item.getModuleType() == null || item.getOperationType() == null)
                throw new ServiceException("变更项缺少模块或操作类型");
            String module = item.getModuleType(), operation = item.getOperationType();
            if (!List.of("PROJECT", "WBS", "TASK", "DELIVERABLE", "TEAM").contains(module))
                throw new ServiceException("变更模块不受支持");
            if (!List.of("ADD", "UPDATE", "DELETE").contains(operation))
                throw new ServiceException("变更项操作类型无效");
            if ("PROJECT".equals(module) && !"UPDATE".equals(operation))
                throw new ServiceException("项目计划仅支持修改");
            if (item.getTargetType() == null || item.getTargetType().isBlank())
                throw new ServiceException("变更项缺少目标类型");
            if (!"ADD".equals(operation)
                    && !("PROJECT".equals(module) && "UPDATE".equals(operation))
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
                    objectMapper.readValue(item.getAfterJson(), Map.class);
                } catch (Exception e) {
                    throw new ServiceException("变更后内容格式无效");
                }
            }
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

    private void applyItem(ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        if (item.getModuleType() == null || item.getOperationType() == null)
            throw new ServiceException("变更项缺少模块或操作类型");
        if (!"ADD".equals(item.getOperationType())
                && !"UPDATE".equals(item.getOperationType())
                && !"DELETE".equals(item.getOperationType()))
            throw new ServiceException("变更项操作类型无效");
        if ("PROJECT".equals(item.getModuleType()) && "UPDATE".equals(item.getOperationType())) {
            applyProject(change, item, operator);
            return;
        }
        if ("WBS".equals(item.getModuleType())) {
            applyWbs(change, item, operator);
            return;
        }
        if ("TASK".equals(item.getModuleType())) {
            applyTask(change, item, operator);
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
        if ("PROJECT".equals(module)) return 10;
        if ("WBS".equals(module) && !"DELETE".equals(operation)) return 20;
        if ("TASK".equals(module) && !"DELETE".equals(operation)) return 30;
        if ("DELIVERABLE".equals(module) && !"DELETE".equals(operation)) return 40;
        if ("DELIVERABLE".equals(module)) return 60;
        if ("TASK".equals(module)) return 70;
        if ("TEAM".equals(module)) return 80;
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

    private void applyProject(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            ProjectInfo requested = objectMapper.readValue(item.getAfterJson(), ProjectInfo.class);
            ProjectInfo old = requireProject(change.getProjectId());
            java.time.LocalDate start =
                    requested.getStartDate() == null
                            ? old.getStartDate()
                            : requested.getStartDate();
            java.time.LocalDate end =
                    requested.getEndDate() == null ? old.getEndDate() : requested.getEndDate();
            String budgetRequired =
                    requested.getBudgetRequired() == null
                            ? old.getBudgetRequired()
                            : requested.getBudgetRequired();
            java.math.BigDecimal budgetAmount =
                    requested.getBudgetAmount() == null
                            ? old.getBudgetAmount()
                            : requested.getBudgetAmount();
            if (start == null || end == null || end.isBefore(start))
                throw new ServiceException("项目计划日期不能为空且结束日期不能早于开始日期");
            if ("1".equals(budgetRequired) && budgetAmount == null)
                throw new ServiceException("需要预算时必须填写计划预算");
            if (budgetAmount != null && budgetAmount.signum() < 0)
                throw new ServiceException("计划预算不能为负数");
            old.setStartDate(start);
            old.setEndDate(end);
            old.setBudgetRequired(budgetRequired);
            old.setBudgetAmount(budgetAmount);
            old.setBudgetDescription(
                    requested.getBudgetDescription() == null
                            ? old.getBudgetDescription()
                            : requested.getBudgetDescription());
            old.setUpdateBy(operator);
            projectMapper.updateProjectInfo(old);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("项目计划变更数据无效");
        }
    }

    private void applyWbs(ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectWbsNode old = wbsMapper.selectById(item.getTargetId());
                if (old == null || !old.getProjectId().equals(change.getProjectId())) {
                    throw new ServiceException("WBS节点不存在");
                }
                if (wbsMapper.countChildren(old.getWbsId()) > 0
                        || wbsMapper.countTasks(old.getWbsId()) > 0
                        || wbsMapper.countDeliverables(old.getWbsId()) > 0
                        || issueMapper.countByWorkPackage(change.getProjectId(), old.getWbsId())
                                > 0) {
                    throw new ServiceException("WBS节点已有下级或业务记录，不能删除");
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
                ProjectWbsNode parentNode = null;
                if (parent != 0) {
                    parentNode = wbsMapper.selectById(parent);
                    if (parentNode == null
                            || !parentNode.getProjectId().equals(change.getProjectId())) {
                        throw new ServiceException("WBS父级不存在或不属于当前项目");
                    }
                    if ("WORK_PACKAGE".equals(parentNode.getNodeType())) {
                        throw new ServiceException("工作包不能继续挂载WBS节点");
                    }
                }
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
                if (requested.getSortOrder() == null) requested.setSortOrder(siblings.size());
                wbsMapper.insert(requested);
                return;
            }
            ProjectWbsNode old = wbsMapper.selectById(item.getTargetId());
            if (old == null || !old.getProjectId().equals(change.getProjectId())) {
                throw new ServiceException("WBS节点不存在");
            }
            if (requested.getParentId() != null
                    && !requested.getParentId().equals(old.getParentId())) {
                throw new ServiceException("不支持将已有WBS节点移动至其他父级");
            }
            if (requested.getNodeType() != null
                    && !requested.getNodeType().equals(old.getNodeType())) {
                if ("WORK_PACKAGE".equals(requested.getNodeType())
                        && wbsMapper.countChildren(old.getWbsId()) > 0) {
                    throw new ServiceException("有下级节点的WBS不能转换为工作包");
                }
                if ("SUMMARY".equals(requested.getNodeType())
                        && (wbsMapper.countTasks(old.getWbsId()) > 0
                                || wbsMapper.countDeliverables(old.getWbsId()) > 0)) {
                    throw new ServiceException("已有任务或交付物的工作包不能转换为汇总WBS");
                }
            }
            mergeWbs(old, requested);
            validateWbsFields(requested);
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

    private void mergeWbs(ProjectWbsNode old, ProjectWbsNode requested) {
        requested.setWbsId(old.getWbsId());
        requested.setParentId(old.getParentId());
        requested.setWbsCode(old.getWbsCode());
        if (requested.getNodeType() == null) requested.setNodeType(old.getNodeType());
        if (requested.getWbsName() == null) requested.setWbsName(old.getWbsName());
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

    private void applyTask(ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectTask old = taskMapper.selectById(item.getTargetId());
                if (old == null || !old.getProjectId().equals(change.getProjectId()))
                    throw new ServiceException("任务不存在");
                if (taskMapper.countChildren(old.getTaskId()) > 0
                        || old.getActualStartDate() != null
                        || old.getActualEndDate() != null
                        || old.getActualHours() != null
                        || !taskMapper.selectOutputs(old.getTaskId()).isEmpty()
                        || issueMapper.countByTask(change.getProjectId(), old.getTaskId()) > 0) {
                    throw new ServiceException("任务已有下级、成果、问题或执行记录，不能删除");
                }
                taskMapper.delete(old.getTaskId());
                return;
            }
            ProjectTask requested = objectMapper.readValue(item.getAfterJson(), ProjectTask.class);
            requested.setProjectId(change.getProjectId());
            requested.setUpdateBy(operator);
            if ("ADD".equals(item.getOperationType())) {
                Long parent =
                        requested.getParentTaskId() == null ? 0L : requested.getParentTaskId();
                ProjectWbsNode wp = wbsMapper.selectById(requested.getWorkPackageId());
                if (wp == null
                        || !wp.getProjectId().equals(change.getProjectId())
                        || !"WORK_PACKAGE".equals(wp.getNodeType())) {
                    throw new ServiceException("任务必须归属当前项目的工作包");
                }
                if (requested.getTaskName() == null
                        || requested.getTaskName().isBlank()
                        || !("SUMMARY".equals(requested.getTaskType())
                                || "EXECUTION".equals(requested.getTaskType()))) {
                    throw new ServiceException("新增任务必须填写名称和任务类型");
                }
                ProjectTask parentTask = null;
                if (parent != 0) {
                    parentTask = taskMapper.selectById(parent);
                    if (parentTask == null
                            || !parentTask.getProjectId().equals(change.getProjectId())
                            || !requested
                                    .getWorkPackageId()
                                    .equals(parentTask.getWorkPackageId())) {
                        throw new ServiceException("任务父级不存在或不属于同一工作包");
                    }
                    if ("EXECUTION".equals(parentTask.getTaskType()))
                        throw new ServiceException("执行任务不能包含子任务");
                }
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
            ProjectTask old = taskMapper.selectById(item.getTargetId());
            if (old == null || !old.getProjectId().equals(change.getProjectId()))
                throw new ServiceException("任务不存在");
            if ((requested.getParentTaskId() != null
                            && !requested.getParentTaskId().equals(old.getParentTaskId()))
                    || (requested.getWorkPackageId() != null
                            && !requested.getWorkPackageId().equals(old.getWorkPackageId()))) {
                throw new ServiceException("不支持将已有任务移动至其他父级或工作包");
            }
            if (requested.getTaskType() != null
                    && !requested.getTaskType().equals(old.getTaskType())) {
                throw new ServiceException("已有任务不能变更任务类型");
            }
            mergeTask(old, requested);
            validateTaskFields(requested, wbsMapper.selectById(old.getWorkPackageId()));
            taskMapper.update(requested);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("任务变更数据无效");
        }
    }

    private void validateTaskFields(ProjectTask task, ProjectWbsNode wp) {
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
    }

    private void applyDeliverable(
            ProjectPlanChange change, ProjectPlanChangeItem item, String operator) {
        try {
            if ("DELETE".equals(item.getOperationType())) {
                ProjectDeliverable old = deliverableMapper.selectById(item.getTargetId());
                if (old == null || !old.getProjectId().equals(change.getProjectId()))
                    throw new ServiceException("交付物不存在");
                if (deliverableMapper.countSubmissions(old.getDeliverableId()) > 0
                        || !"PENDING".equals(old.getStatus())) {
                    throw new ServiceException("已有提交记录的交付物不能删除");
                }
                deliverableMapper.deleteByIds(new Long[] {old.getDeliverableId()});
                return;
            }
            ProjectDeliverable requested =
                    objectMapper.readValue(item.getAfterJson(), ProjectDeliverable.class);
            requested.setProjectId(change.getProjectId());
            requested.setUpdateBy(operator);
            if ("ADD".equals(item.getOperationType())) {
                ProjectWbsNode wp = wbsMapper.selectById(requested.getWorkPackageId());
                if (wp == null
                        || !wp.getProjectId().equals(change.getProjectId())
                        || !"WORK_PACKAGE".equals(wp.getNodeType())) {
                    throw new ServiceException("交付物必须归属当前项目的工作包");
                }
                if (requested.getDeliverableName() == null
                        || requested.getDeliverableName().isBlank()) {
                    throw new ServiceException("新增交付物必须填写名称");
                }
                applyDeliverableType(requested);
                requested.setCreateBy(operator);
                if (requested.getStatus() == null) requested.setStatus("PENDING");
                deliverableMapper.insert(requested);
                return;
            }
            ProjectDeliverable old = deliverableMapper.selectById(item.getTargetId());
            if (old == null || !old.getProjectId().equals(change.getProjectId()))
                throw new ServiceException("交付物不存在");
            if (requested.getWorkPackageId() != null
                    && !requested.getWorkPackageId().equals(old.getWorkPackageId())) {
                throw new ServiceException("不支持将已有交付物移动至其他工作包");
            }
            requested.setDeliverableId(item.getTargetId());
            requested.setWorkPackageId(old.getWorkPackageId());
            if (requested.getDeliverableName() == null)
                requested.setDeliverableName(old.getDeliverableName());
            if (requested.getDeliverableType() == null
                    && requested.getDeliverableTypeId() == null) {
                requested.setDeliverableType(old.getDeliverableType());
                requested.setDeliverableTypeId(old.getDeliverableTypeId());
            }
            if (requested.getDescription() == null) requested.setDescription(old.getDescription());
            if (requested.getPlannedDate() == null) requested.setPlannedDate(old.getPlannedDate());
            if (requested.getAcceptanceCriteria() == null)
                requested.setAcceptanceCriteria(old.getAcceptanceCriteria());
            if (requested.getSubmissionMode() == null)
                requested.setSubmissionMode(old.getSubmissionMode());
            if (requested.getAllowedExtensions() == null)
                requested.setAllowedExtensions(old.getAllowedExtensions());
            if (requested.getRequiredFlag() == null)
                requested.setRequiredFlag(old.getRequiredFlag());
            if (requested.getApprovalRequired() == null)
                requested.setApprovalRequired(old.getApprovalRequired());
            requested.setStatus(old.getStatus());
            applyDeliverableType(requested);
            deliverableMapper.update(requested);
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("交付物变更数据无效");
        }
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
                ProjectMember old = teamMapper.selectMemberById(item.getTargetId());
                if (old == null || !old.getProjectId().equals(change.getProjectId()))
                    throw new ServiceException("项目成员不存在");
                if ("PROJECT_MANAGER".equals(old.getRoleCode()))
                    throw new ServiceException("项目负责人不能退出团队");
                if (teamMapper.countIncompleteTasks(change.getProjectId(), old.getPersonId()) > 0)
                    throw new ServiceException("成员仍有未完成任务，不能退出");
                old.setUpdateBy(operator);
                old.setExitDate(java.time.LocalDate.now());
                teamMapper.exitMember(old);
                return;
            }
            ProjectMember n = objectMapper.readValue(item.getAfterJson(), ProjectMember.class);
            n.setProjectId(change.getProjectId());
            if ("ADD".equals(item.getOperationType())) {
                if (n.getPersonId() == null || n.getRoleId() == null)
                    throw new ServiceException("新增项目成员必须指定用户和项目角色");
                if (!validRole(change.getProjectId(), n.getRoleId()))
                    throw new ServiceException("项目角色不存在或不属于当前项目");
                if (teamMapper.selectActiveMember(change.getProjectId(), n.getPersonId()) != null)
                    throw new ServiceException("该用户已经是项目活动成员");
                n.setStatus("ACTIVE");
                n.setCreateBy(operator);
                if (n.getJoinDate() == null) n.setJoinDate(java.time.LocalDate.now());
                teamMapper.insertMember(n);
            } else {
                ProjectMember old = teamMapper.selectMemberById(item.getTargetId());
                if (old == null || !old.getProjectId().equals(change.getProjectId()))
                    throw new ServiceException("项目成员不存在");
                if (n.getPersonId() != null && !n.getPersonId().equals(old.getPersonId()))
                    throw new ServiceException("不支持通过变更单替换既有成员");
                if (n.getRoleId() != null && !validRole(change.getProjectId(), n.getRoleId()))
                    throw new ServiceException("项目角色不存在或不属于当前项目");
                n.setMemberId(item.getTargetId());
                n.setPersonId(old.getPersonId());
                if (n.getProfessionalRoleId() == null)
                    n.setProfessionalRoleId(old.getProfessionalRoleId());
                if (n.getSpecialtyRole() == null) n.setSpecialtyRole(old.getSpecialtyRole());
                if (n.getResponsibility() == null) n.setResponsibility(old.getResponsibility());
                if (n.getRemark() == null) n.setRemark(old.getRemark());
                n.setUpdateBy(operator);
                teamMapper.updateMember(n);
            }
        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            throw new ServiceException("项目团队变更数据无效");
        }
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
