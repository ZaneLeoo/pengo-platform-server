package com.ruoyi.projectmanagement.execution.service;

import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import java.util.List;
import java.util.Map;

/** 项目执行项业务。 */
public interface IProjectWorkItemService {
    List<ProjectWorkItem> selectList(ProjectWorkItem item);
    ProjectWorkItem selectById(Long itemId);
    int insert(ProjectWorkItem item);
    int update(ProjectWorkItem item);
    int deleteByIds(Long[] itemIds);
    List<Map<String, Object>> overview();
}
