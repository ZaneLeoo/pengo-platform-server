package com.ruoyi.flow.engine.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.flow.binding.domain.FlowBinding;
import com.ruoyi.flow.binding.mapper.FlowBindingMapper;
import com.ruoyi.flow.definition.domain.FlowDefinition;
import com.ruoyi.flow.definition.domain.FlowDefinitionNode;
import com.ruoyi.flow.definition.enums.FlowSignType;
import com.ruoyi.flow.definition.mapper.FlowDefinitionMapper;
import com.ruoyi.flow.definition.mapper.FlowDefinitionNodeMapper;
import com.ruoyi.flow.engine.domain.FlowHistory;
import com.ruoyi.flow.engine.domain.FlowInstance;
import com.ruoyi.flow.engine.domain.FlowTask;
import com.ruoyi.flow.engine.enums.FlowAction;
import com.ruoyi.flow.engine.enums.FlowInstanceStatus;
import com.ruoyi.flow.engine.enums.FlowTaskStatus;
import com.ruoyi.flow.engine.mapper.FlowHistoryMapper;
import com.ruoyi.flow.engine.mapper.FlowInstanceMapper;
import com.ruoyi.flow.engine.mapper.FlowTaskMapper;
import com.ruoyi.flow.handler.FlowFinishedEvent;
import com.ruoyi.flow.message.domain.FlowMessage;
import com.ruoyi.flow.message.mapper.FlowMessageMapper;
import com.ruoyi.system.service.ISysUserService;
import java.util.Date;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批流程引擎：发起、同意、驳回、撤销与节点推进。
 * 流程为线性审批链，节点支持或签（任一同意即通过）与会签（全部同意才通过）。
 */
@Service
public class FlowEngineService {

    private final FlowBindingMapper bindingMapper;
    private final FlowDefinitionMapper definitionMapper;
    private final FlowDefinitionNodeMapper nodeMapper;
    private final FlowInstanceMapper instanceMapper;
    private final FlowTaskMapper taskMapper;
    private final FlowHistoryMapper historyMapper;
    private final FlowMessageMapper messageMapper;
    private final ISysUserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public FlowEngineService(FlowBindingMapper bindingMapper, FlowDefinitionMapper definitionMapper,
            FlowDefinitionNodeMapper nodeMapper, FlowInstanceMapper instanceMapper, FlowTaskMapper taskMapper,
            FlowHistoryMapper historyMapper, FlowMessageMapper messageMapper, ISysUserService userService,
            ApplicationEventPublisher eventPublisher) {
        this.bindingMapper = bindingMapper;
        this.definitionMapper = definitionMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.historyMapper = historyMapper;
        this.messageMapper = messageMapper;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发起流程：按业务类型找到绑定的启用流程，创建实例并生成首个审批节点任务。
     *
     * @return 创建的流程实例
     */
    @Transactional
    public FlowInstance start(String bizType, Long bizId, String bizCode, String bizName, String operator) {
        FlowBinding binding = bindingMapper.selectByBizType(bizType);
        if (binding == null) {
            throw new ServiceException("业务类型【" + bizType + "】未绑定审批流程，请联系管理员配置");
        }
        FlowDefinition definition = definitionMapper.selectById(binding.getFlowId());
        if (definition == null) {
            throw new ServiceException("绑定的审批流程不存在");
        }
        if (!"1".equals(definition.getStatus())) {
            throw new ServiceException("审批流程【" + definition.getFlowName() + "】未启用，无法发起");
        }
        List<FlowDefinitionNode> nodes = nodeMapper.selectByFlowId(definition.getFlowId());
        if (nodes.isEmpty()) {
            throw new ServiceException("审批流程【" + definition.getFlowName() + "】未配置审批节点");
        }
        FlowInstance existing = instanceMapper.selectByBiz(bizType, bizId);
        if (existing != null && FlowInstanceStatus.RUNNING.matches(existing.getStatus())) {
            throw new ServiceException("该业务已存在审批中的流程，请勿重复提交");
        }
        FlowDefinitionNode first = nodes.get(0);
        FlowInstance instance = new FlowInstance();
        instance.setFlowId(definition.getFlowId());
        instance.setFlowKey(definition.getFlowKey());
        instance.setFlowName(definition.getFlowName());
        instance.setBizType(bizType);
        instance.setBizId(bizId);
        instance.setBizCode(bizCode);
        instance.setBizName(bizName);
        instance.setCurrentNodeId(first.getNodeId());
        instance.setCurrentNodeName(first.getNodeName());
        instance.setStatus(FlowInstanceStatus.RUNNING.getCode());
        instance.setSubmitBy(operator);
        instance.setSubmitName(nickName(operator));
        instance.setSubmitTime(new Date());
        instanceMapper.insert(instance);

        createTasks(instance, first, operator);
        recordHistory(instance, null, null, FlowAction.SUBMIT, operator, null);
        return instance;
    }

    /**
     * 同意当前待办：或签节点任一同意即推进，会签节点需全部同意才推进。
     */
    @Transactional
    public void approve(Long taskId, String comment, String operator) {
        FlowTask task = pendingTask(taskId, operator);
        FlowInstance instance = requiredInstance(task.getInstanceId());
        if (!FlowInstanceStatus.RUNNING.matches(instance.getStatus())) {
            throw new ServiceException("流程已结束，不能继续审批");
        }
        finishTask(task, FlowTaskStatus.APPROVED, comment, operator);
        recordHistory(instance, task.getNodeId(), task.getNodeName(), FlowAction.APPROVE, operator, comment);

        List<FlowDefinitionNode> nodes = nodeMapper.selectByFlowId(instance.getFlowId());
        FlowDefinitionNode node = nodes.stream().filter(x -> x.getNodeId().equals(task.getNodeId())).findFirst()
                .orElse(null);
        if (node == null) {
            throw new ServiceException("审批节点不存在");
        }
        boolean orSign = FlowSignType.OR.matches(node.getSignType());
        List<FlowTask> pendingInNode = taskMapper.selectPendingByInstance(instance.getInstanceId()).stream()
                .filter(x -> x.getNodeId().equals(node.getNodeId()))
                .toList();
        if (orSign) {
            // 或签：本任务同意后节点即通过，同节点其余待办跳过
            if (!pendingInNode.isEmpty()) {
                for (FlowTask pending : pendingInNode) {
                    skipTask(pending, operator);
                }
            }
            advance(instance, nodes, node);
        } else {
            // 会签：等待同节点全部任务处理完再推进
            if (pendingInNode.isEmpty()) {
                advance(instance, nodes, node);
            }
        }
    }

    /** 驳回当前待办：实例直接进入驳回终态并回调业务。 */
    @Transactional
    public void reject(Long taskId, String comment, String operator) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new ServiceException("驳回时必须填写意见");
        }
        FlowTask task = pendingTask(taskId, operator);
        FlowInstance instance = requiredInstance(task.getInstanceId());
        if (!FlowInstanceStatus.RUNNING.matches(instance.getStatus())) {
            throw new ServiceException("流程已结束，不能继续审批");
        }
        finishTask(task, FlowTaskStatus.REJECTED, comment, operator);
        recordHistory(instance, task.getNodeId(), task.getNodeName(), FlowAction.REJECT, operator, comment);
        for (FlowTask pending : taskMapper.selectPendingByInstance(instance.getInstanceId())) {
            skipTask(pending, operator);
        }
        finishInstance(instance, FlowInstanceStatus.REJECTED);
        notifyHandler(instance, comment, operator, false);
    }

    /** 撤销流程：仅发起人可撤销审批中的实例。 */
    @Transactional
    public void cancel(Long instanceId, String operator) {
        FlowInstance instance = requiredInstance(instanceId);
        if (!FlowInstanceStatus.RUNNING.matches(instance.getStatus())) {
            throw new ServiceException("流程已结束，不能撤销");
        }
        if (!instance.getSubmitBy().equalsIgnoreCase(operator) && !"admin".equalsIgnoreCase(operator)) {
            throw new ServiceException("只有发起人可以撤销流程");
        }
        for (FlowTask pending : taskMapper.selectPendingByInstance(instanceId)) {
            skipTask(pending, operator);
        }
        recordHistory(instance, null, null, FlowAction.CANCEL, operator, null);
        finishInstance(instance, FlowInstanceStatus.CANCELLED);
        eventPublisher.publishEvent(new FlowFinishedEvent(instance, false, null, operator));
    }

    /** 查询实例审批链条（按时间正序）。 */
    public List<FlowHistory> history(Long instanceId) {
        return historyMapper.selectByInstance(instanceId);
    }

    /** 按业务查询最新实例。 */
    public FlowInstance instanceByBiz(String bizType, Long bizId) {
        return instanceMapper.selectByBiz(bizType, bizId);
    }

    /** 查询流程定义详情。 */
    public FlowInstance getInstance(Long instanceId) {
        return requiredInstance(instanceId);
    }

    /** 节点通过后推进到下一节点或完成实例。 */
    private void advance(FlowInstance instance, List<FlowDefinitionNode> nodes, FlowDefinitionNode current) {
        FlowDefinitionNode next = nodes.stream()
                .filter(x -> x.getSortOrder() > current.getSortOrder())
                .findFirst()
                .orElse(null);
        if (next == null) {
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);
            finishInstance(instance, FlowInstanceStatus.APPROVED);
            notifyHandler(instance, null, instance.getSubmitBy(), true);
            return;
        }
        instance.setCurrentNodeId(next.getNodeId());
        instance.setCurrentNodeName(next.getNodeName());
        instanceMapper.updateCurrentNode(instance);
        createTasks(instance, next, instance.getSubmitBy());
    }

    /** 为节点创建各审批人的待办任务并发送站内消息。 */
    private void createTasks(FlowInstance instance, FlowDefinitionNode node, String operator) {
        for (String assignee : resolveAssignees(node)) {
            FlowTask task = new FlowTask();
            task.setInstanceId(instance.getInstanceId());
            task.setNodeId(node.getNodeId());
            task.setNodeName(node.getNodeName());
            task.setAssignee(assignee);
            task.setAssigneeName(nickName(assignee));
            task.setStatus(FlowTaskStatus.PENDING.getCode());
            taskMapper.insert(task);
            notifyMessage(assignee, instance, node);
        }
    }

    /** 解析节点审批人登录名列表。 */
    private List<String> resolveAssignees(FlowDefinitionNode node) {
        if (!"user".equals(node.getAssignType())) {
            throw new ServiceException("节点【" + node.getNodeName() + "】的审批人配置类型暂不支持：" + node.getAssignType());
        }
        return java.util.Arrays.stream(node.getAssignValue().split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .distinct()
                .toList();
    }

    /** 校验任务待审批且属于当前操作者。 */
    private FlowTask pendingTask(Long taskId, String operator) {
        FlowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("待办任务不存在");
        }
        if (!FlowTaskStatus.PENDING.matches(task.getStatus())) {
            throw new ServiceException("该任务已处理，请刷新列表");
        }
        if (!task.getAssignee().equalsIgnoreCase(operator)) {
            throw new ServiceException("该任务不属于当前用户，不能处理");
        }
        return task;
    }

    /** 完成单条任务。 */
    private void finishTask(FlowTask task, FlowTaskStatus status, String comment, String operator) {
        task.setStatus(status.getCode());
        task.setComment(comment);
        task.setApproveTime(new Date());
        taskMapper.updateStatus(task);
    }

    /** 跳过单条任务（或签通过后同节点其余任务、驳回/撤销后的剩余任务）。 */
    private void skipTask(FlowTask task, String operator) {
        task.setStatus(FlowTaskStatus.SKIPPED.getCode());
        task.setComment("流程节点已由他人处理");
        task.setApproveTime(new Date());
        taskMapper.updateStatus(task);
    }

    /** 实例进入终态并记录完成时间。 */
    private void finishInstance(FlowInstance instance, FlowInstanceStatus status) {
        instance.setCurrentNodeId(null);
        instance.setCurrentNodeName(null);
        instance.setStatus(status.getCode());
        instance.setFinishTime(new Date());
        instanceMapper.updateFinish(instance);
    }

    /** 记录审批历史。 */
    private void recordHistory(FlowInstance instance, Long nodeId, String nodeName, FlowAction action,
            String operator, String comment) {
        FlowHistory history = new FlowHistory();
        history.setInstanceId(instance.getInstanceId());
        history.setNodeId(nodeId);
        history.setNodeName(nodeName);
        history.setAction(action.getCode());
        history.setOperator(operator);
        history.setOperatorName(nickName(operator));
        history.setComment(comment);
        historyMapper.insert(history);
    }

    /** 发送站内消息给审批人。 */
    private void notifyMessage(String receiver, FlowInstance instance, FlowDefinitionNode node) {
        FlowMessage message = new FlowMessage();
        message.setReceiver(receiver);
        message.setTitle("您有一条待审批任务：" + instance.getFlowName());
        message.setContent("【" + instance.getBizName() + "】等待您在节点【" + node.getNodeName() + "】处理");
        message.setBizType(instance.getBizType());
        message.setBizId(instance.getBizId());
        message.setInstanceId(instance.getInstanceId());
        messageMapper.insert(message);
    }

    /** 实例终态时发布事件通知业务模块。 */
    private void notifyHandler(FlowInstance instance, String comment, String operator, boolean approved) {
        eventPublisher.publishEvent(new FlowFinishedEvent(instance, approved, comment, operator));
    }

    /** 查询登录名对应的姓名。 */
    private String nickName(String loginName) {
        try {
            var user = userService.selectUserByUserName(loginName);
            return user == null ? loginName : user.getNickName();
        } catch (Exception e) {
            return loginName;
        }
    }

    /** 查询实例，不存在时抛异常。 */
    private FlowInstance requiredInstance(Long instanceId) {
        FlowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ServiceException("流程实例不存在");
        }
        return instance;
    }
}
