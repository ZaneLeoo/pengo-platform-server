package com.ruoyi.projectmanagement.task.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.common.enums.LifecycleAction;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.TaskType;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.common.enums.WorkItemStatus;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOperationLog;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOutput;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 任务树与任务执行业务实现。 */
@Service
public class ProjectTaskServiceImpl implements IProjectTaskService {

    private final ProjectTaskMapper mapper;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectInfoMapper projectMapper;
    private final IProjectTeamService teamService;
    private final IProjectWbsService wbsService;
    private final ProjectDeliverableMapper deliverableMapper;

    public ProjectTaskServiceImpl(
            ProjectTaskMapper mapper,
            ProjectWbsMapper wbsMapper,
            ProjectInfoMapper projectMapper,
            IProjectTeamService teamService,
            IProjectWbsService wbsService,
            ProjectDeliverableMapper deliverableMapper) {
        this.mapper = mapper;
        this.wbsMapper = wbsMapper;
        this.projectMapper = projectMapper;
        this.teamService = teamService;
        this.wbsService = wbsService;
        this.deliverableMapper = deliverableMapper;
    }

    /** 查询任务列表。 */
    @Override
    public List<ProjectTask> list(ProjectTask filter) {
        return mapper.selectList(filter);
    }

    /** 查询当前登录人员被分配的执行任务。 */
    @Override
    public List<ProjectTask> listMine(Long userId, ProjectTask filter) {
        if (filter == null) {
            filter = new ProjectTask();
        }
        filter.setAssigneeUserId(userId);
        filter.setTaskType(TaskType.EXECUTION.getCode());
        return mapper.selectList(filter);
    }

    /** 查询任务详细，不存在时抛异常。 */
    @Override
    public ProjectTask get(Long id) {
        return required(id);
    }

    /** 新增任务，生成层级编码并刷新工作包汇总。 */
    @Override
    @Transactional
    public Long add(ProjectTask task, String operator) {
        assertStructureMutable(task.getProjectId());
        normalize(task);
        ProjectWbsNode workPackage = workPackage(task.getWorkPackageId(), task.getProjectId());
        validate(task, workPackage);
        task.setTaskCode(nextCode(workPackage, task.getParentTaskId()));
        task.setStatus(WorkItemStatus.NOT_STARTED.getCode());
        task.setProgress(0);
        task.setCreateBy(operator);
        if (mapper.insert(task) == 0) {
            throw new ServiceException("新增任务失败");
        }
        refreshPackage(task.getWorkPackageId());
        return task.getTaskId();
    }

    /** 修改任务，所属项目、工作包、上级与类型不允许变更。 */
    @Override
    @Transactional
    public int edit(ProjectTask task, String operator) {
        ProjectTask old = required(task.getTaskId());
        assertStructureMutable(old.getProjectId());
        task.setProjectId(old.getProjectId());
        task.setWorkPackageId(old.getWorkPackageId());
        task.setParentTaskId(old.getParentTaskId());
        task.setTaskType(old.getTaskType());
        task.setUpdateBy(operator);
        validate(task, workPackage(old.getWorkPackageId(), old.getProjectId()));
        int rows = mapper.update(task);
        refreshPackage(old.getWorkPackageId());
        return rows;
    }

    /** 删除任务，汇总任务有下级或任务已有成果时不允许删除。 */
    @Override
    @Transactional
    public int remove(Long id, String operator) {
        ProjectTask task = required(id);
        assertStructureMutable(task.getProjectId());
        if (mapper.countChildren(id) > 0) {
            throw new ServiceException("汇总任务仍有下级，不能删除");
        }
        if (!mapper.selectOutputs(id).isEmpty()) {
            throw new ServiceException("任务已有成果，不能删除");
        }
        int rows = mapper.delete(id);
        refreshPackage(task.getWorkPackageId());
        return rows;
    }

    /** 执行任务生命周期动作（开始/暂停/恢复/完成）。 */
    @Override
    @Transactional
    public int lifecycle(Long id, LifecycleActionRequest request, String operator, Long userId) {
        ProjectTask task = required(id);
        if (!TaskType.EXECUTION.matches(task.getTaskType())) {
            throw new ServiceException("汇总任务由下级自动汇总，不能执行生命周期动作");
        }
        assertTaskExecutor(task, userId);
        ProjectInfo project = projectMapper.selectProjectInfoById(task.getProjectId());
        if (project == null || !ProjectStatus.ACTIVE.matches(project.getStatus())) {
            throw new ServiceException("项目未执行中，不能执行任务动作");
        }
        LifecycleAction action = LifecycleAction.fromCode(request.getAction());
        if (action == null) {
            throw new ServiceException("不支持的任务动作");
        }
        String from = task.getStatus();
        String to;
        switch (action) {
            case START -> {
                if (!WorkItemStatus.NOT_STARTED.matches(from)) {
                    throw new ServiceException("只有未开始任务可以开始");
                }
                to = WorkItemStatus.ACTIVE.getCode();
                task.setActualStartDate(LocalDate.now());
            }
            case PAUSE -> {
                if (!WorkItemStatus.ACTIVE.matches(from)
                        || StringUtils.isBlank(request.getReason())) {
                    throw new ServiceException("只有进行中任务可以暂停，且必须填写原因");
                }
                to = WorkItemStatus.PAUSED.getCode();
                task.setPauseReason(request.getReason());
            }
            case RESUME -> {
                if (!WorkItemStatus.PAUSED.matches(from)) {
                    throw new ServiceException("只有已暂停任务可以恢复");
                }
                to = WorkItemStatus.ACTIVE.getCode();
                task.setPauseReason(null);
            }
            case COMPLETE -> {
                if (!WorkItemStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有进行中任务可以完成");
                }
                to = WorkItemStatus.COMPLETED.getCode();
                task.setActualEndDate(LocalDate.now());
                task.setProgress(100);
            }
            default -> throw new ServiceException("不支持的任务动作");
        }
        task.setStatus(to);
        task.setUpdateBy(operator);
        int rows = mapper.updateLifecycle(task);
        ProjectTaskOperationLog log = new ProjectTaskOperationLog();
        log.setTaskId(task.getTaskId());
        log.setAction(action.getCode());
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setRemark(LifecycleAction.PAUSE == action ? request.getReason() : null);
        log.setOperatorUserId(userId);
        log.setOperatorName(operator);
        log.setOperationTime(LocalDateTime.now());
        mapper.insertOperationLog(log);
        refreshPackage(task.getWorkPackageId());
        return rows;
    }

    /** 查询执行任务的生命周期记录。 */
    @Override
    public List<ProjectTaskOperationLog> operationLogs(Long taskId) {
        required(taskId);
        return mapper.selectOperationLogs(taskId);
    }

    /** 查询任务成果列表。 */
    @Override
    public List<ProjectTaskOutput> outputs(Long id) {
        required(id);
        return mapper.selectOutputs(id);
    }

    /** 新增任务成果，仅执行任务且任务未完成时可上传。 */
    @Override
    @Transactional
    public int addOutput(ProjectTaskOutput output, String operator, Long userId) {
        ProjectTask task = required(output.getTaskId());
        if (!TaskType.EXECUTION.matches(task.getTaskType())) {
            throw new ServiceException("只有执行任务可以上传任务成果");
        }
        if (WorkItemStatus.COMPLETED.matches(task.getStatus())) {
            throw new ServiceException("已完成任务的成果仅可查看");
        }
        assertTaskExecutor(task, userId);
        output.setCreateBy(operator);
        return mapper.insertOutput(output);
    }

    /** 删除任务成果，仅任务执行人可操作。 */
    @Override
    @Transactional
    public int removeOutput(Long id, String operator, Long userId) {
        ProjectTaskOutput output = mapper.selectOutput(id);
        if (output == null) {
            throw new ServiceException("任务成果不存在");
        }
        ProjectTask task = required(output.getTaskId());
        if (WorkItemStatus.COMPLETED.matches(task.getStatus())) {
            throw new ServiceException("已完成任务的成果仅可查看");
        }
        assertTaskExecutor(task, userId);
        return mapper.deleteOutput(id);
    }

    /** 刷新工作包汇总：汇总任务按下级均值聚合，工作包状态按任务完成度与交付物要求推进。 */
    @Override
    @Transactional
    public void refreshPackage(Long packageId) {
        ProjectWbsNode workPackage = wbsMapper.selectById(packageId);
        if (workPackage == null) {
            return;
        }
        ProjectTask filter = new ProjectTask();
        filter.setWorkPackageId(packageId);
        List<ProjectTask> all = mapper.selectList(filter);
        all.stream()
                .sorted(Comparator.comparingInt(x -> -depth(x, all)))
                .filter(x -> TaskType.SUMMARY.matches(x.getTaskType()))
                .forEach(
                        x -> {
                            List<ProjectTask> children =
                                    all.stream()
                                            .filter(y -> x.getTaskId().equals(y.getParentTaskId()))
                                            .toList();
                            int progress =
                                    children.isEmpty()
                                            ? 0
                                            : (int)
                                                    Math.round(
                                                            children.stream()
                                                                    .mapToInt(
                                                                            y ->
                                                                                    y.getProgress()
                                                                                                    == null
                                                                                            ? 0
                                                                                            : y
                                                                                                    .getProgress())
                                                                    .average()
                                                                    .orElse(0));
                            String status =
                                    progress == 100
                                            ? WorkItemStatus.COMPLETED.getCode()
                                            : children.stream()
                                                            .anyMatch(
                                                                    y ->
                                                                            !WorkItemStatus
                                                                                    .NOT_STARTED
                                                                                    .matches(
                                                                                            y
                                                                                                    .getStatus()))
                                                    ? WorkItemStatus.ACTIVE.getCode()
                                                    : WorkItemStatus.NOT_STARTED.getCode();
                            mapper.updateAggregate(x.getTaskId(), status, progress);
                            x.setStatus(status);
                            x.setProgress(progress);
                        });
        List<ProjectTask> roots = all.stream().filter(x -> x.getParentTaskId() == 0).toList();
        int progress =
                roots.isEmpty()
                        ? 0
                        : (int)
                                Math.round(
                                        roots.stream()
                                                .mapToInt(
                                                        x ->
                                                                x.getProgress() == null
                                                                        ? 0
                                                                        : x.getProgress())
                                                .average()
                                                .orElse(0));
        boolean tasksDone =
                !roots.isEmpty()
                        && roots.stream()
                                .allMatch(x -> WorkItemStatus.COMPLETED.matches(x.getStatus()));
        boolean delivered =
                tasksDone
                        && deliverableMapper.countUnsatisfiedRequiredByWorkPackageId(packageId)
                                == 0;
        String status =
                delivered
                        ? WbsStatus.COMPLETED.getCode()
                        : tasksDone
                                ? WbsStatus.WAITING_DELIVERY.getCode()
                                : progress > 0
                                        ? WbsStatus.ACTIVE.getCode()
                                        : WbsStatus.NOT_STARTED.getCode();
        if (delivered) {
            progress = 100;
        } else if (tasksDone) {
            progress = 99;
        }
        wbsMapper.updateAggregate(
                packageId,
                workPackage.getPlanStartDate(),
                workPackage.getPlanEndDate(),
                status,
                progress);
        wbsService.refreshProject(workPackage.getProjectId());
    }

    /** 校验任务结构：上级任务归属、执行任务必须配置在组执行人与计划日期。 */
    private void validate(ProjectTask task, ProjectWbsNode workPackage) {
        if (task.getParentTaskId() != 0) {
            ProjectTask parent = required(task.getParentTaskId());
            if (!workPackage.getWbsId().equals(parent.getWorkPackageId())) {
                throw new ServiceException("上级任务不属于当前工作包");
            }
            if (TaskType.EXECUTION.matches(parent.getTaskType())) {
                throw new ServiceException("执行任务不能包含子任务");
            }
        }
        if (TaskType.EXECUTION.matches(task.getTaskType())) {
            if (task.getAssigneeId() == null
                    || !teamService.isActiveMember(task.getProjectId(), task.getAssigneeId())) {
                throw new ServiceException("任务执行人必须是当前项目在组成员");
            }
            if (task.getPlanStartDate() == null || task.getPlanEndDate() == null) {
                throw new ServiceException("请填写执行任务计划日期");
            }
            if (task.getPlanEndDate().isBefore(task.getPlanStartDate())) {
                throw new ServiceException("任务结束日期不能早于开始日期");
            }
            if (task.getPlanStartDate().isBefore(workPackage.getPlanStartDate())
                    || task.getPlanEndDate().isAfter(workPackage.getPlanEndDate())) {
                throw new ServiceException(
                        "任务计划日期必须在工作包周期内（"
                                + workPackage.getPlanStartDate()
                                + " ~ "
                                + workPackage.getPlanEndDate()
                                + "）");
            }
            if (task.getParentTaskId() != 0) {
                ProjectTask parent = required(task.getParentTaskId());
                if (parent.getPlanStartDate() != null
                        && (task.getPlanStartDate().isBefore(parent.getPlanStartDate())
                                || task.getPlanEndDate().isAfter(parent.getPlanEndDate()))) {
                    throw new ServiceException(
                            "子任务计划日期必须在上级任务周期内（"
                                    + parent.getPlanStartDate()
                                    + " ~ "
                                    + parent.getPlanEndDate()
                                    + "）");
                }
            }
        }
    }

    /** 校验任务所属工作包存在且属于指定项目。 */
    private ProjectWbsNode workPackage(Long id, Long projectId) {
        ProjectWbsNode node = wbsMapper.selectById(id);
        if (node == null
                || !projectId.equals(node.getProjectId())
                || !WbsNodeType.WORK_PACKAGE.matches(node.getNodeType())) {
            throw new ServiceException("所属工作包不存在");
        }
        return node;
    }

    /** 按工作包与上级任务生成任务编码，如 1-T1、1.1、1.1.2。 */
    private String nextCode(ProjectWbsNode workPackage, Long parentId) {
        List<ProjectTask> children = mapper.selectChildren(workPackage.getWbsId(), parentId);
        String prefix =
                parentId == 0
                        ? workPackage.getWbsCode() + "-T"
                        : required(parentId).getTaskCode() + ".";
        return prefix + (children.size() + 1);
    }

    /** 补齐默认值。 */
    private void normalize(ProjectTask task) {
        if (task.getParentTaskId() == null) {
            task.setParentTaskId(0L);
        }
        if (task.getSortOrder() == null) {
            task.setSortOrder(0);
        }
    }

    /** 计算任务在树中的深度。 */
    private int depth(ProjectTask task, List<ProjectTask> all) {
        int depth = 0;
        Long parentId = task.getParentTaskId();
        while (parentId != null && parentId != 0) {
            depth++;
            Long id = parentId;
            parentId =
                    all.stream()
                            .filter(x -> x.getTaskId().equals(id))
                            .map(ProjectTask::getParentTaskId)
                            .findFirst()
                            .orElse(0L);
        }
        return depth;
    }

    /** 查询任务，不存在时抛异常。 */
    private ProjectTask required(Long id) {
        ProjectTask task = mapper.selectById(id);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return task;
    }

    /** 仅任务执行人可推进任务及维护任务成果。 */
    private void assertTaskExecutor(ProjectTask task, Long userId) {
        if (!userId.equals(task.getAssigneeUserId())) {
            throw new ServiceException("只有任务执行人可以执行此操作");
        }
    }

    /** 校验项目处于已立项待启动状态，允许调整任务结构。 */
    private void assertStructureMutable(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null || !ProjectStatus.APPROVED.matches(project.getStatus())) {
            throw new ServiceException("只有已立项待启动项目可以调整任务结构");
        }
    }
}
