package com.ruoyi.projectmanagement.workflow.mapper;

import com.ruoyi.projectmanagement.workflow.domain.WorkflowDefinition;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowInstance;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowTask;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 轻量审批流数据访问。 */
@Mapper
public interface WorkflowMapper {
    List<WorkflowDefinition> selectDefinitions();

    WorkflowDefinition selectDefinition(Long definitionId);

    WorkflowDefinition selectActiveDefinition(String businessType);

    WorkflowDefinition selectDefinitionVersion(Long versionId);

    String selectRoleName(String roleCode);

    int insertDefinition(WorkflowDefinition definition);

    int updateDefinition(WorkflowDefinition definition);

    int insertVersion(
            @Param("definition") WorkflowDefinition definition,
            @Param("graphJson") String graphJson);

    int publishVersion(@Param("versionId") Long versionId, @Param("operator") String operator);

    int activateVersion(
            @Param("definitionId") Long definitionId,
            @Param("versionId") Long versionId,
            @Param("operator") String operator);

    int insertInstance(WorkflowInstance instance);

    WorkflowInstance selectInstance(Long instanceId);

    WorkflowInstance selectInstanceByBusiness(
            @Param("businessType") String businessType, @Param("businessId") Long businessId);

    int insertTask(WorkflowTask task);

    int insertCandidate(@Param("taskId") Long taskId, @Param("userId") Long userId);

    List<Long> selectRoleUsers(
            @Param("projectId") Long projectId, @Param("roleCode") String roleCode);

    WorkflowTask selectTask(Long taskId);

    int canViewTask(@Param("taskId") Long taskId, @Param("userId") Long userId);

    int canViewInstance(@Param("instanceId") Long instanceId, @Param("userId") Long userId);

    List<WorkflowTask> selectTasks(Long instanceId);

    List<WorkflowTask> selectInbox(@Param("userId") Long userId, @Param("scope") String scope);

    int markRead(@Param("taskId") Long taskId, @Param("userId") Long userId);

    int countUnread(Long userId);

    int actTask(
            @Param("taskId") Long taskId,
            @Param("status") String status,
            @Param("userId") Long userId,
            @Param("opinion") String opinion);

    int activateNextTask(Long instanceId);

    int cancelWaitingTasks(Long instanceId);

    int cancelPendingTasks(Long instanceId);

    int finishInstance(
            @Param("instanceId") Long instanceId,
            @Param("status") String status,
            @Param("operator") String operator);

    int insertEvent(
            @Param("instanceId") Long instanceId,
            @Param("taskId") Long taskId,
            @Param("eventType") String eventType,
            @Param("userId") Long userId,
            @Param("content") String content);
}
