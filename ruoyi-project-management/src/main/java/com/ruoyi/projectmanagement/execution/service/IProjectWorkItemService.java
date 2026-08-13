package com.ruoyi.projectmanagement.execution.service;

import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import java.util.List;
import java.util.Map;

/**
 * 项目执行项（WBS任务、交付物、问题）业务接口。
 */
public interface IProjectWorkItemService {

    /** 查询执行项列表。 */
    List<ProjectWorkItem> selectList(ProjectWorkItem item);

    /** 根据ID查询执行项。 */
    ProjectWorkItem selectById(Long itemId);

    /** 新增执行项。 */
    int insert(ProjectWorkItem item);

    /** 修改执行项。 */
    int update(ProjectWorkItem item);

    /** 批量删除执行项。 */
    int deleteByIds(Long[] itemIds);

    /** 执行项状态统计概览。 */
    List<Map<String, Object>> overview();

    /** 执行项生命周期动作（开始/暂停/恢复/完成）。 */
    int applyLifecycleAction(Long itemId, LifecycleActionRequest request, String operator);
}
