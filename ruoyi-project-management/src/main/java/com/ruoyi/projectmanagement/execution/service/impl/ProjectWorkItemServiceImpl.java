package com.ruoyi.projectmanagement.execution.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.common.enums.ProjectPhaseStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WorkItemStatus;
import com.ruoyi.projectmanagement.common.enums.WorkItemType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.execution.service.IProjectWorkItemService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import com.ruoyi.projectmanagement.phase.mapper.ProjectPhaseMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 项目执行项业务实现。
 */
@Service
public class ProjectWorkItemServiceImpl implements IProjectWorkItemService {

    private final ProjectWorkItemMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectDeliverableMapper deliverableMapper;
    private final IProjectTeamService teamService;
    private final ProjectPhaseMapper phaseMapper;

    public ProjectWorkItemServiceImpl(ProjectWorkItemMapper mapper, ProjectInfoMapper projectMapper,
            ProjectDeliverableMapper deliverableMapper, IProjectTeamService teamService,
            ProjectPhaseMapper phaseMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.deliverableMapper = deliverableMapper;
        this.teamService = teamService;
        this.phaseMapper = phaseMapper;
    }

    @Override
    public List<ProjectWorkItem> selectList(ProjectWorkItem item) {
        return mapper.selectList(item);
    }

    @Override
    public ProjectWorkItem selectById(Long itemId) {
        return mapper.selectById(itemId);
    }

    @Override
    public List<Map<String, Object>> overview() {
        return mapper.selectOverview();
    }

    @Override
    public int deleteByIds(Long[] itemIds) {
        for (Long itemId : itemIds) {
            ProjectWorkItem item = mapper.selectById(itemId);
            if (item != null) {
                assertFormalModuleAllowed(item.getProjectId(), item.getItemType());
            }
            if (item != null && WorkItemType.TASK.matches(item.getItemType())
                    && WorkItemStatus.COMPLETED.matches(item.getStatus())) {
                throw new ServiceException("已完成任务仅可查看，不能删除；如需调整请先重新打开任务");
            }
        }
        return mapper.deleteByIds(itemIds);
    }

    @Override
    public int insert(ProjectWorkItem item) {
        assertFormalModuleAllowed(item.getProjectId(), item.getItemType());
        if (WorkItemType.TASK.matches(item.getItemType()) && item.getParentId() != null && item.getParentId() != 0) {
            ProjectWorkItem parent = mapper.selectById(item.getParentId());
            if (parent == null || !WorkItemType.TASK.matches(parent.getItemType())) {
                throw new ServiceException("上级WBS任务不存在");
            }
            if (WorkItemStatus.COMPLETED.matches(parent.getStatus())) {
                throw new ServiceException("已完成任务不能新增子任务；如需调整请先重新打开任务");
            }
            item.setPhaseId(parent.getPhaseId());
        }
        if (WorkItemType.TASK.matches(item.getItemType())) {
            item.setStatus(WorkItemStatus.NOT_STARTED.getCode());
        }
        validate(item);
        return mapper.insert(item);
    }

    @Override
    public int update(ProjectWorkItem item) {
        ProjectWorkItem existing = mapper.selectById(item.getItemId());
        if (existing == null) {
            throw new ServiceException("项目执行项不存在");
        }
        assertFormalModuleAllowed(existing.getProjectId(), existing.getItemType());
        if (WorkItemType.TASK.matches(item.getItemType())) {
        if (WorkItemStatus.COMPLETED.matches(existing.getStatus())) {
                throw new ServiceException("已完成任务仅可查看，不能编辑；如需调整请先重新打开任务");
            }
            item.setStatus(existing.getStatus());
            item.setActualStartDate(existing.getActualStartDate());
            item.setActualEndDate(existing.getActualEndDate());
            item.setPauseReason(existing.getPauseReason());
        }
        validate(item);
        return mapper.update(item);
    }

    @Override
    public int applyLifecycleAction(Long itemId, LifecycleActionRequest request, String operator) {
        ProjectWorkItem item = mapper.selectById(itemId);
        if (item == null || !WorkItemType.TASK.matches(item.getItemType())) {
            throw new ServiceException("WBS任务不存在");
        }
        assertFormalModuleAllowed(item.getProjectId(), item.getItemType());
        if (!"admin".equalsIgnoreCase(operator)
                && (StringUtils.isBlank(item.getOwnerCode()) || !item.getOwnerCode().equalsIgnoreCase(operator))) {
            throw new ServiceException("只有任务负责人或admin可以执行该任务动作");
        }
        ProjectInfo project = projectMapper.selectProjectInfoById(item.getProjectId());
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        String from = item.getStatus();
        String action = request.getAction().trim().toUpperCase();
        String to;
        switch (action) {
            case "START" -> {
                if (!WorkItemStatus.NOT_STARTED.matches(from)) {
                    throw new ServiceException("只有未开始任务可以开始");
                }
                if (!ProjectStatus.ACTIVE.matches(project.getStatus())) {
                    throw new ServiceException("项目未执行中，不能开始新任务");
                }
                assertPhaseActive(item);
                to = WorkItemStatus.ACTIVE.getCode();
                item.setActualStartDate(LocalDate.now());
            }
            case "PAUSE" -> {
                if (!WorkItemStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的任务可以暂停");
                }
                if (StringUtils.isBlank(request.getReason())) {
                    throw new ServiceException("暂停任务必须填写原因");
                }
                to = WorkItemStatus.PAUSED.getCode();
                item.setPauseReason(request.getReason().trim());
            }
            case "RESUME" -> {
                if (!WorkItemStatus.PAUSED.matches(from)) {
                    throw new ServiceException("只有已暂停任务可以恢复");
                }
                if (!ProjectStatus.ACTIVE.matches(project.getStatus())) {
                    throw new ServiceException("项目未执行中，不能恢复任务");
                }
                assertPhaseActive(item);
                to = WorkItemStatus.ACTIVE.getCode();
                item.setPauseReason(null);
            }
            case "COMPLETE" -> {
                if (!WorkItemStatus.ACTIVE.matches(from)) {
                    throw new ServiceException("只有执行中的任务可以完成");
                }
                if (mapper.countChildren(itemId) > 0) {
                    throw new ServiceException("汇总任务由下级任务状态汇总，不能手工完成");
                }
                if (deliverableMapper.countUnsatisfiedRequiredByTaskId(itemId) > 0
                        || ("1".equals(item.getDeliverableRequired())
                                && deliverableMapper.countByTaskId(itemId) == 0)) {
                    throw new ServiceException("该任务要求交付物，请先完成所有必交交付物");
                }
                to = WorkItemStatus.COMPLETED.getCode();
                item.setActualEndDate(LocalDate.now());
                item.setProgress(100);
            }
            default -> throw new ServiceException("不支持的任务生命周期动作");
        }
        item.setStatus(to);
        item.setUpdateBy(operator);
        int rows = mapper.updateLifecycle(item);
        if (rows > 0) {
            mapper.insertLifecycleLog(itemId, item.getProjectId(), action, from, to, request.getReason(), operator);
        }
        if (rows > 0 && WorkItemStatus.COMPLETED.matches(to)) {
            refreshSummaryProgress(item.getProjectId());
        }
        return rows;
    }

    private void validate(ProjectWorkItem item) {
        if (item.getDeliverableRequired() == null) {
            item.setDeliverableRequired("0");
        }
        if (projectMapper.selectProjectInfoById(item.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (WorkItemType.TASK.matches(item.getItemType()) && item.getOwnerId() == null) {
            throw new ServiceException("请选择WBS任务负责人");
        }
        if (WorkItemType.TASK.matches(item.getItemType())
                && !teamService.isActiveMember(item.getProjectId(), item.getOwnerId())) {
            throw new ServiceException("任务负责人必须是当前项目的在组成员");
        }
        if (WorkItemType.TASK.matches(item.getItemType())) {
            if (item.getParentId() != null && item.getParentId() != 0) {
                ProjectWorkItem parent = mapper.selectById(item.getParentId());
                if (parent == null || !item.getProjectId().equals(parent.getProjectId())) {
                    throw new ServiceException("上级WBS任务不存在或不属于当前项目");
                }
                item.setPhaseId(parent.getPhaseId());
            }
            if (item.getPhaseId() == null) {
                throw new ServiceException("请选择所属项目阶段");
            }
            ProjectPhase phase = phaseMapper.selectById(item.getPhaseId());
            if (phase == null || !item.getProjectId().equals(phase.getProjectId())) {
                throw new ServiceException("所属阶段不存在或不属于当前项目");
            }
            if (ProjectPhaseStatus.COMPLETED.matches(phase.getStatus())) {
                throw new ServiceException("已完成阶段不能新增或修改WBS任务");
            }
            if (item.getStartDate() == null || item.getDueDate() == null) {
                throw new ServiceException("请完整填写WBS任务计划开始和结束日期");
            }
            if (item.getStartDate().isBefore(phase.getStartDate())
                    || item.getDueDate().isAfter(phase.getEndDate())) {
                throw new ServiceException("WBS任务计划日期必须在所属阶段计划周期内");
            }
            if (item.getParentId() != null && item.getParentId() != 0) {
                ProjectWorkItem parent = mapper.selectById(item.getParentId());
                if (parent.getStartDate() == null || parent.getDueDate() == null
                        || item.getStartDate().isBefore(parent.getStartDate())
                        || item.getDueDate().isAfter(parent.getDueDate())) {
                    throw new ServiceException("子任务计划日期必须在上级任务计划周期内");
                }
            }
        }
        if (WorkItemType.DELIVERABLE.matches(item.getItemType()) && item.getTaskId() == null) {
            throw new ServiceException("请选择关联WBS任务");
        }
        if (item.getTaskId() != null) {
            ProjectWorkItem task = mapper.selectById(item.getTaskId());
            if (task == null || !WorkItemType.TASK.matches(task.getItemType())
                    || !task.getProjectId().equals(item.getProjectId())) {
                throw new ServiceException("关联WBS任务不存在或不属于当前项目");
            }
        }
        if (WorkItemType.TASK.matches(item.getItemType()) && WorkItemStatus.COMPLETED.matches(item.getStatus())
                && (deliverableMapper.countUnsatisfiedRequiredByTaskId(item.getItemId()) > 0
                        || ("1".equals(item.getDeliverableRequired())
                                && deliverableMapper.countByTaskId(item.getItemId()) == 0))) {
            throw new ServiceException("该任务要求交付物，请先完成所有必交交付物");
        }
        ProjectWorkItem sameCode = mapper.selectByCode(item.getItemCode());
        if (sameCode != null && !sameCode.getItemId().equals(item.getItemId())) {
            throw new ServiceException("执行项编码已存在");
        }
        if (item.getStartDate() != null && item.getDueDate() != null
                && item.getDueDate().isBefore(item.getStartDate())) {
            throw new ServiceException("截止日期不能早于开始日期");
        }
        if (item.getProgress() == null) {
            item.setProgress(0);
        }
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        if (item.getParentId() == null) {
            item.setParentId(0L);
        }
    }

    private void assertPhaseActive(ProjectWorkItem item) {
        ProjectPhase phase = phaseMapper.selectById(item.getPhaseId());
        if (phase == null || !ProjectPhaseStatus.ACTIVE.matches(phase.getStatus())) {
            throw new ServiceException("所属阶段未进行中，请先开始阶段");
        }
    }

    private void assertFormalModuleAllowed(Long projectId, String itemType) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (ProjectStatus.DRAFT.matches(project.getStatus()) || ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            String module = WorkItemType.ISSUE.matches(itemType) ? "问题跟踪" : "项目计划";
            throw new ServiceException("项目尚未正式立项，不能维护" + module);
        }
    }

    private void refreshSummaryProgress(Long projectId) {
        ProjectWorkItem filter = new ProjectWorkItem();
        filter.setProjectId(projectId);
        filter.setItemType(WorkItemType.TASK.getCode());
        List<ProjectWorkItem> tasks = mapper.selectList(filter);
        Map<Long, List<ProjectWorkItem>> children = new HashMap<>();
        for (ProjectWorkItem task : tasks) {
            if (task.getParentId() != null && task.getParentId() != 0) {
                children.computeIfAbsent(task.getParentId(), ignored -> new ArrayList<>()).add(task);
            }
        }
        for (ProjectWorkItem task : tasks) {
            if (children.containsKey(task.getItemId())) {
                mapper.updateProgress(task.getItemId(), summaryProgress(task, children));
            }
        }
    }

    private int summaryProgress(ProjectWorkItem task, Map<Long, List<ProjectWorkItem>> children) {
        List<ProjectWorkItem> direct = children.get(task.getItemId());
        if (direct == null || direct.isEmpty()) {
            return task.getProgress() == null ? 0 : task.getProgress();
        }
        int total = 0;
        int count = 0;
        for (ProjectWorkItem child : direct) {
            if (children.containsKey(child.getItemId())) {
                total += summaryProgress(child, children);
            } else {
                total += WorkItemStatus.COMPLETED.matches(child.getStatus()) ? 100 : 0;
            }
            count++;
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }
}
