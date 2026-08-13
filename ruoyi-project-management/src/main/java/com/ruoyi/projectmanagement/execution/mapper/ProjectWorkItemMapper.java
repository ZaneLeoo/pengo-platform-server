package com.ruoyi.projectmanagement.execution.mapper;

import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目执行项数据访问接口。
 */
@Mapper
public interface ProjectWorkItemMapper {

    /** 查询执行项列表。 */
    List<ProjectWorkItem> selectList(ProjectWorkItem item);

    /** 根据ID查询执行项。 */
    ProjectWorkItem selectById(Long itemId);

    /** 根据编码查询执行项。 */
    ProjectWorkItem selectByCode(String itemCode);

    /** 查询同一上级下的直接WBS任务，用于自动生成层级编码。 */
    List<ProjectWorkItem> selectDirectTasks(@Param("projectId") Long projectId, @Param("parentId") Long parentId);

    /** 新增执行项。 */
    int insert(ProjectWorkItem item);

    /** 修改执行项。 */
    int update(ProjectWorkItem item);

    /** 批量删除执行项。 */
    int deleteByIds(Long[] itemIds);

    /** 统计任务关联的交付物数量。 */
    int countDeliverablesByTaskId(Long taskId);

    /** 执行项状态统计概览。 */
    List<Map<String, Object>> selectOverview();

    /** 更新执行项生命周期状态。 */
    int updateLifecycle(ProjectWorkItem item);

    /** 更新任务汇总进度。 */
    int updateProgress(@Param("itemId") Long itemId, @Param("progress") int progress);

    /** 记录执行项生命周期操作日志。 */
    int insertLifecycleLog(@Param("itemId") Long itemId, @Param("projectId") Long projectId,
            @Param("action") String action, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("reason") String reason,
            @Param("operator") String operator);

    /** 统计任务子节点数量。 */
    int countChildren(@Param("itemId") Long itemId);

    /** 统计阶段下的末级任务数量。 */
    int countLeafTasksByPhaseId(@Param("phaseId") Long phaseId);

    /** 统计项目未完成的末级任务数量。 */
    int countIncompleteLeafTasksByProjectId(@Param("projectId") Long projectId);
}
