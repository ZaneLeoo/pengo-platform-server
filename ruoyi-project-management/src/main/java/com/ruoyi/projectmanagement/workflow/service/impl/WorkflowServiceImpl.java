package com.ruoyi.projectmanagement.workflow.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowActionRequest;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowDefinition;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowInstance;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowNode;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowTask;
import com.ruoyi.projectmanagement.workflow.mapper.WorkflowMapper;
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import com.ruoyi.projectmanagement.workflow.service.WorkflowBusinessCallback;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 只支持严格串行节点的轻量审批流实现。 */
@Service
public class WorkflowServiceImpl implements IWorkflowService {
    private final WorkflowMapper mapper;
    private final ObjectMapper objectMapper;
    private final Map<String, WorkflowBusinessCallback> callbacks;

    public WorkflowServiceImpl(
            WorkflowMapper mapper,
            ObjectMapper objectMapper,
            List<WorkflowBusinessCallback> callbacks) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.callbacks =
                callbacks.stream()
                        .collect(
                                Collectors.toMap(
                                        WorkflowBusinessCallback::businessType,
                                        Function.identity()));
    }

    @Override
    public List<WorkflowDefinition> definitions() {
        List<WorkflowDefinition> definitions = mapper.selectDefinitions();
        definitions.forEach(this::deserializeNodes);
        return definitions;
    }

    @Override
    public WorkflowDefinition definition(Long id) {
        WorkflowDefinition definition = mapper.selectDefinition(id);
        if (definition == null) {
            throw new ServiceException("审批流程不存在");
        }
        deserializeNodes(definition);
        return definition;
    }

    @Override
    @Transactional
    public WorkflowDefinition saveDraft(WorkflowDefinition definition, String operator) {
        validateNodes(definition.getNodes());
        definition.setCreateBy(operator);
        definition.setUpdateBy(operator);
        if (definition.getDefinitionId() == null) {
            WorkflowDefinition existing =
                    mapper.selectDefinitionByBusinessType(definition.getBusinessType());
            if (existing != null) {
                throw new ServiceException("该业务类型已存在审批流程，请直接编辑现有流程");
            }
            mapper.insertDefinition(definition);
        } else {
            WorkflowDefinition existing =
                    mapper.selectDefinitionByBusinessType(definition.getBusinessType());
            if (existing != null && !existing.getDefinitionId().equals(definition.getDefinitionId())) {
                throw new ServiceException("该业务类型已存在其他审批流程，不能重复配置");
            }
            mapper.updateDefinition(definition);
        }
        mapper.insertVersion(definition, serializeNodes(definition.getNodes()));
        return definition(definition.getDefinitionId());
    }

    @Override
    @Transactional
    public void publish(Long definitionId, Long versionId, String operator) {
        WorkflowDefinition definition = definition(definitionId);
        if (!versionId.equals(definition.getVersionId())
                || !"DRAFT".equals(definition.getVersionStatus())) {
            throw new ServiceException("只能发布当前草稿版本");
        }
        if (mapper.publishVersion(versionId, operator) != 1) {
            throw new ServiceException("流程版本已发布或不存在");
        }
        mapper.activateVersion(definitionId, versionId, operator);
    }

    @Override
    @Transactional
    public Long start(
            String businessType,
            Long businessId,
            Long projectId,
            String title,
            String snapshot,
            String operator,
            Long initiatorUserId) {
        WorkflowDefinition definition = mapper.selectActiveDefinition(businessType);
        if (definition == null) {
            throw new ServiceException("该业务尚未配置并发布审批流程");
        }
        deserializeNodes(definition);
        List<List<Long>> candidates =
                resolveCandidates(definition.getNodes(), projectId, initiatorUserId);
        WorkflowInstance instance = new WorkflowInstance();
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setProjectId(projectId);
        instance.setDefinitionVersionId(definition.getVersionId());
        instance.setTitle(title);
        instance.setInitiatorUserId(initiatorUserId);
        instance.setStatus("RUNNING");
        instance.setCurrentNodeKey(definition.getNodes().get(0).getKey());
        instance.setBusinessSnapshotJson(snapshot);
        instance.setCreateBy(operator);
        mapper.insertInstance(instance);
        createTasks(instance.getInstanceId(), definition.getNodes(), candidates);
        mapper.insertEvent(instance.getInstanceId(), null, "STARTED", initiatorUserId, "发起审批");
        return instance.getInstanceId();
    }

    @Override
    public List<WorkflowTask> tasks(Long userId, String scope) {
        return mapper.selectInbox(userId, scope);
    }

    @Override
    @Transactional
    public WorkflowInstance taskDetail(Long taskId, Long userId) {
        WorkflowTask task = requireTask(taskId);
        if (mapper.canViewTask(taskId, userId) == 0) {
            throw new ServiceException("您无权查看该审批任务");
        }
        mapper.markRead(taskId, userId);
        WorkflowInstance instance = mapper.selectInstance(task.getInstanceId());
        instance.setTasks(enrichTasks(instance, mapper.selectTasks(instance.getInstanceId())));
        return instance;
    }

    /** 查询完整审批实例，供业务历史记录展示流程图。 */
    @Override
    public WorkflowInstance instanceDetail(Long instanceId, Long userId) {
        if (mapper.canViewInstance(instanceId, userId) == 0) {
            throw new ServiceException("您无权查看该审批流程");
        }
        WorkflowInstance instance = mapper.selectInstance(instanceId);
        if (instance == null) {
            throw new ServiceException("审批流程不存在");
        }
        instance.setTasks(enrichTasks(instance, mapper.selectTasks(instanceId)));
        return instance;
    }

    /** 使用实例绑定的流程版本补充审批来源；候选人则始终读取实例创建时的固定快照。 */
    private List<WorkflowTask> enrichTasks(WorkflowInstance instance, List<WorkflowTask> tasks) {
        WorkflowDefinition version =
                mapper.selectDefinitionVersion(instance.getDefinitionVersionId());
        if (version == null) {
            return tasks;
        }
        deserializeNodes(version);
        Map<String, WorkflowNode> nodes =
                version.getNodes().stream()
                        .collect(Collectors.toMap(WorkflowNode::getKey, Function.identity()));
        tasks.forEach(
                task -> {
                    WorkflowNode node = nodes.get(task.getNodeKey());
                    if (node == null) {
                        return;
                    }
                    task.setApproverType(node.getApproverType());
                    String roleName =
                            "PROJECT_ROLE".equals(node.getApproverType())
                                    ? mapper.selectRoleName(node.getApproverValue())
                                    : null;
                    task.setApproverLabel(
                            "PROJECT_ROLE".equals(node.getApproverType())
                                    ? (StringUtils.isNotBlank(roleName)
                                            ? roleName
                                            : node.getApproverValue())
                                    : "指定用户");
                });
        return tasks;
    }

    @Override
    public int unreadCount(Long userId) {
        return mapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void act(Long taskId, WorkflowActionRequest request, String operator, Long userId) {
        WorkflowTask task = requireTask(taskId);
        boolean approve = "APPROVE".equals(request.getAction().toUpperCase(Locale.ROOT));
        if (!approve && !"REJECT".equals(request.getAction().toUpperCase(Locale.ROOT))) {
            throw new ServiceException("审批动作仅支持同意或驳回");
        }
        if (!approve && StringUtils.isBlank(request.getOpinion())) {
            throw new ServiceException("驳回时必须填写意见");
        }
        String result = approve ? "APPROVED" : "REJECTED";
        if (mapper.actTask(taskId, result, userId, request.getOpinion()) != 1) {
            throw new ServiceException("任务已处理或您不是当前审批人");
        }
        mapper.insertEvent(task.getInstanceId(), taskId, result, userId, request.getOpinion());
        finishOrAdvance(task, approve, operator, request.getOpinion(), userId);
    }

    @Override
    @Transactional
    public void withdraw(Long instanceId, String operator, Long userId) {
        WorkflowInstance instance = mapper.selectInstance(instanceId);
        if (instance == null || !"RUNNING".equals(instance.getStatus())) {
            throw new ServiceException("审批流程已结束，不能撤回");
        }
        if (!userId.equals(instance.getInitiatorUserId())) {
            throw new ServiceException("仅审批发起人可以撤回");
        }
        mapper.cancelWaitingTasks(instanceId);
        mapper.cancelPendingTasks(instanceId);
        mapper.finishInstance(instanceId, "WITHDRAWN", operator);
        mapper.insertEvent(instanceId, null, "WITHDRAWN", userId, "发起人撤回审批");
    }

    private void finishOrAdvance(
            WorkflowTask task,
            boolean approve,
            String operator,
            String opinion,
            Long operatorUserId) {
        WorkflowInstance instance = mapper.selectInstance(task.getInstanceId());
        if (!approve) {
            mapper.cancelWaitingTasks(instance.getInstanceId());
            mapper.finishInstance(instance.getInstanceId(), "REJECTED", operator);
            callback(instance)
                    .rejected(
                            instance.getBusinessId(),
                            operator,
                            opinion,
                            operatorUserId,
                            instance.getInstanceId());
            return;
        }
        if (mapper.activateNextTask(instance.getInstanceId()) == 0) {
            mapper.finishInstance(instance.getInstanceId(), "APPROVED", operator);
            callback(instance)
                    .approved(
                            instance.getBusinessId(),
                            operator,
                            opinion,
                            operatorUserId,
                            instance.getInstanceId());
        }
    }

    private WorkflowBusinessCallback callback(WorkflowInstance instance) {
        WorkflowBusinessCallback callback = callbacks.get(instance.getBusinessType());
        if (callback == null) {
            throw new ServiceException("审批业务回写处理器不存在");
        }
        return callback;
    }

    private void createTasks(
            Long instanceId, List<WorkflowNode> nodes, List<List<Long>> candidates) {
        for (int index = 0; index < nodes.size(); index++) {
            WorkflowNode node = nodes.get(index);
            WorkflowTask task = new WorkflowTask();
            task.setInstanceId(instanceId);
            task.setNodeKey(node.getKey());
            task.setNodeName(node.getName());
            task.setNodeOrder(index + 1);
            task.setStatus(index == 0 ? "PENDING" : "WAITING");
            mapper.insertTask(task);
            for (Long userId : candidates.get(index)) {
                mapper.insertCandidate(task.getTaskId(), userId);
            }
        }
    }

    private List<List<Long>> resolveCandidates(
            List<WorkflowNode> nodes, Long projectId, Long initiatorUserId) {
        List<List<Long>> resolved = new ArrayList<>();
        for (WorkflowNode node : nodes) {
            List<Long> users =
                    "USER".equals(node.getApproverType())
                            ? List.of(parseUserId(node.getApproverValue()))
                            : mapper.selectRoleUsers(projectId, node.getApproverValue());
            List<Long> filtered =
                    users.stream().filter(id -> !id.equals(initiatorUserId)).distinct().toList();
            if (filtered.isEmpty()) {
                throw new ServiceException("节点“" + node.getName() + "”排除发起人后没有可用审批人");
            }
            resolved.add(filtered);
        }
        return resolved;
    }

    private Long parseUserId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new ServiceException("指定审批人的用户ID无效");
        }
    }

    private void validateNodes(List<WorkflowNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new ServiceException("审批流程至少需要一个审批节点");
        }
        Set<String> keys = new HashSet<>();
        for (WorkflowNode node : nodes) {
            if (StringUtils.isBlank(node.getKey())
                    || StringUtils.isBlank(node.getName())
                    || !keys.add(node.getKey())) {
                throw new ServiceException("审批节点名称、键不能为空且键不可重复");
            }
            if (!"USER".equals(node.getApproverType())
                    && !"PROJECT_ROLE".equals(node.getApproverType())) {
                throw new ServiceException("审批人类型仅支持指定用户或项目角色");
            }
            if (StringUtils.isBlank(node.getApproverValue())) {
                throw new ServiceException("每个审批节点都必须配置审批人");
            }
        }
    }

    private WorkflowTask requireTask(Long taskId) {
        WorkflowTask task = mapper.selectTask(taskId);
        if (task == null) {
            throw new ServiceException("审批任务不存在");
        }
        return task;
    }

    private String serializeNodes(List<WorkflowNode> nodes) {
        try {
            return objectMapper.writeValueAsString(nodes);
        } catch (JacksonException exception) {
            throw new ServiceException("审批流程配置无法序列化");
        }
    }

    private void deserializeNodes(WorkflowDefinition definition) {
        try {
            definition.setNodes(
                    objectMapper.readValue(definition.getGraphJson(), new TypeReference<>() {}));
        } catch (JacksonException exception) {
            throw new ServiceException("审批流程配置损坏");
        }
    }
}
