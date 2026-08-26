package com.ruoyi.projectmanagement.task.service;

import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOperationLog;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOutput;
import java.util.List;

/** 任务树与任务执行业务。 */
public interface IProjectTaskService {

    /** 查询任务列表。 */
    List<ProjectTask> list(ProjectTask filter);

    /** 查询当前登录人员被分配的执行任务。 */
    List<ProjectTask> listMine(Long userId, ProjectTask filter);

    /** 查询任务详细。 */
    ProjectTask get(Long id);

    /** 新增任务，返回新任务ID。 */
    Long add(ProjectTask task, String operator);

    /** 修改任务。 */
    int edit(ProjectTask task, String operator);

    /** 删除任务。 */
    int remove(Long id, String operator);

    /** 执行任务生命周期动作。 */
    int lifecycle(Long id, LifecycleActionRequest request, String operator, Long userId);

    /** 查询任务成果列表。 */
    List<ProjectTaskOutput> outputs(Long taskId);

    /** 新增任务成果。 */
    int addOutput(ProjectTaskOutput output, String operator, Long userId);

    /** 删除任务成果。 */
    int removeOutput(Long id, String operator, Long userId);

    /** 查询执行任务的操作历史。 */
    List<ProjectTaskOperationLog> operationLogs(Long taskId);

    /** 刷新工作包汇总状态与进度。 */
    void refreshPackage(Long packageId);
}
