package com.ruoyi.projectmanagement.execution.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.execution.service.IProjectWorkItemService;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/** 项目执行项业务实现。 */
@Service
public class ProjectWorkItemServiceImpl implements IProjectWorkItemService {
    private final ProjectWorkItemMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectDeliverableMapper deliverableMapper;
    private final IProjectTeamService teamService;

    public ProjectWorkItemServiceImpl(ProjectWorkItemMapper mapper, ProjectInfoMapper projectMapper, ProjectDeliverableMapper deliverableMapper, IProjectTeamService teamService) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.deliverableMapper = deliverableMapper;
        this.teamService = teamService;
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
            if (item != null && "TASK".equals(item.getItemType()) && "COMPLETED".equals(item.getStatus())) {
                throw new ServiceException("已完成任务仅可查看，不能删除；如需调整请先重新打开任务");
            }
        }
        return mapper.deleteByIds(itemIds);
    }

    @Override
    public int insert(ProjectWorkItem item) {
        if ("TASK".equals(item.getItemType()) && item.getParentId() != null && item.getParentId() != 0) {
            ProjectWorkItem parent = mapper.selectById(item.getParentId());
            if (parent == null || !"TASK".equals(parent.getItemType())) throw new ServiceException("上级WBS任务不存在");
            if ("COMPLETED".equals(parent.getStatus())) throw new ServiceException("已完成任务不能新增子任务；如需调整请先重新打开任务");
        }
        if ("TASK".equals(item.getItemType())) item.setStatus("NOT_STARTED");
        validate(item);
        return mapper.insert(item);
    }

    @Override
    public int update(ProjectWorkItem item) {
        ProjectWorkItem existing = mapper.selectById(item.getItemId());
        if (existing == null) throw new ServiceException("项目执行项不存在");
        if ("TASK".equals(item.getItemType())) {
            if ("COMPLETED".equals(existing.getStatus())) {
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
        if (item == null || !"TASK".equals(item.getItemType())) throw new ServiceException("WBS任务不存在");
        if (!"admin".equalsIgnoreCase(operator) && (StringUtils.isBlank(item.getOwnerCode()) || !item.getOwnerCode().equalsIgnoreCase(operator))) {
            throw new ServiceException("只有任务负责人或admin可以执行该任务动作");
        }
        var project = projectMapper.selectProjectInfoById(item.getProjectId());
        if (project == null) throw new ServiceException("所属项目不存在");
        String from = item.getStatus();
        String action = request.getAction().trim().toUpperCase();
        String to;
        switch (action) {
            case "START" -> {
                if (!"NOT_STARTED".equals(from)) throw new ServiceException("只有未开始任务可以开始");
                if (!"ACTIVE".equals(project.getStatus())) throw new ServiceException("项目未执行中，不能开始新任务");
                to = "ACTIVE";
                item.setActualStartDate(LocalDate.now());
            }
            case "PAUSE" -> {
                if (!"ACTIVE".equals(from)) throw new ServiceException("只有执行中的任务可以暂停");
                if (StringUtils.isBlank(request.getReason())) throw new ServiceException("暂停任务必须填写原因");
                to = "PAUSED";
                item.setPauseReason(request.getReason().trim());
            }
            case "RESUME" -> {
                if (!"PAUSED".equals(from)) throw new ServiceException("只有已暂停任务可以恢复");
                if (!"ACTIVE".equals(project.getStatus())) throw new ServiceException("项目未执行中，不能恢复任务");
                to = "ACTIVE";
                item.setPauseReason(null);
            }
            case "COMPLETE" -> {
                if (!"ACTIVE".equals(from)) throw new ServiceException("只有执行中的任务可以完成");
                if (deliverableMapper.countUnsatisfiedRequiredByTaskId(itemId) > 0
                    || ("1".equals(item.getDeliverableRequired()) && deliverableMapper.countByTaskId(itemId) == 0)) {
                    throw new ServiceException("该任务要求交付物，请先完成所有必交交付物");
                }
                to = "COMPLETED";
                item.setActualEndDate(LocalDate.now());
                item.setProgress(100);
            }
            default -> throw new ServiceException("不支持的任务生命周期动作");
        }
        item.setStatus(to);
        item.setUpdateBy(operator);
        int rows = mapper.updateLifecycle(item);
        if (rows > 0) mapper.insertLifecycleLog(itemId, item.getProjectId(), action, from, to, request.getReason(), operator);
        return rows;
    }

    private void validate(ProjectWorkItem item) {
        if (item.getDeliverableRequired() == null) {
            item.setDeliverableRequired("0");
        }
        if (projectMapper.selectProjectInfoById(item.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
        }
        if ("TASK".equals(item.getItemType()) && item.getOwnerId() != null && !teamService.isActiveMember(item.getProjectId(), item.getOwnerId())) {
            throw new ServiceException("任务负责人必须是当前项目的在组成员");
        }
        if ("DELIVERABLE".equals(item.getItemType()) && item.getTaskId() == null) {
            throw new ServiceException("请选择关联WBS任务");
        }
        if (item.getTaskId() != null) {
            ProjectWorkItem task = mapper.selectById(item.getTaskId());
            if (task == null || !"TASK".equals(task.getItemType())
                    || !task.getProjectId().equals(item.getProjectId())) {
                throw new ServiceException("关联WBS任务不存在或不属于当前项目");
            }
        }
        if ("TASK".equals(item.getItemType()) && "COMPLETED".equals(item.getStatus())
                && (deliverableMapper.countUnsatisfiedRequiredByTaskId(item.getItemId()) > 0
                        || ("1".equals(item.getDeliverableRequired()) && deliverableMapper.countByTaskId(item.getItemId()) == 0))) {
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
        if (item.getProgress() == null)
            item.setProgress(0);
        if (item.getSortOrder() == null)
            item.setSortOrder(0);
        if (item.getParentId() == null)
            item.setParentId(0L);
    }
}
