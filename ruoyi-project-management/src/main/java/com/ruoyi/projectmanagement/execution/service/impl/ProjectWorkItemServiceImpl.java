package com.ruoyi.projectmanagement.execution.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.execution.service.IProjectWorkItemService;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 项目执行项业务实现。 */
@Service
public class ProjectWorkItemServiceImpl implements IProjectWorkItemService {
    private final ProjectWorkItemMapper mapper;
    private final ProjectInfoMapper projectMapper;

    public ProjectWorkItemServiceImpl(ProjectWorkItemMapper mapper, ProjectInfoMapper projectMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
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
        return mapper.deleteByIds(itemIds);
    }

    @Override
    public int insert(ProjectWorkItem item) {
        validate(item);
        return mapper.insert(item);
    }

    @Override
    public int update(ProjectWorkItem item) {
        validate(item);
        return mapper.update(item);
    }

    private void validate(ProjectWorkItem item) {
        if (item.getDeliverableRequired() == null) {
            item.setDeliverableRequired("0");
        }
        if (projectMapper.selectProjectInfoById(item.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
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
        if ("TASK".equals(item.getItemType()) && "1".equals(item.getDeliverableRequired())
                && "COMPLETED".equals(item.getStatus()) && mapper.countDeliverablesByTaskId(item.getItemId()) == 0) {
            throw new ServiceException("该任务要求交付物，请先创建关联交付物再完成任务");
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
