package com.ruoyi.projectmanagement.workflow.service;

import com.ruoyi.projectmanagement.workflow.domain.WorkflowActionRequest;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowDefinition;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowInstance;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowTask;
import java.util.List;

/** 轻量串行审批流服务。 */
public interface IWorkflowService {
    List<WorkflowDefinition> definitions();

    WorkflowDefinition definition(Long id);

    WorkflowDefinition saveDraft(WorkflowDefinition definition, String operator);

    void publish(Long definitionId, Long versionId, String operator);

    Long start(
            String businessType,
            Long businessId,
            Long projectId,
            String title,
            String snapshot,
            String operator,
            Long initiatorUserId);

    List<WorkflowTask> tasks(Long userId, String scope);

    WorkflowInstance taskDetail(Long taskId, Long userId);

    WorkflowInstance instanceDetail(Long instanceId, Long userId);

    int unreadCount(Long userId);

    void act(Long taskId, WorkflowActionRequest request, String operator, Long userId);

    /** 发起人撤回仍在审批中的业务，同时取消所有未处理审批任务。 */
    void withdraw(Long instanceId, String operator, Long userId);
}
