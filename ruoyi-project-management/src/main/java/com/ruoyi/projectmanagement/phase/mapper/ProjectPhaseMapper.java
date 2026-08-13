package com.ruoyi.projectmanagement.phase.mapper;

import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目阶段数据访问接口。
 */
@Mapper
public interface ProjectPhaseMapper {

    /** 查询项目阶段列表。 */
    List<ProjectPhase> selectList(ProjectPhase phase);

    /** 根据ID查询阶段。 */
    ProjectPhase selectById(Long id);

    /** 新增阶段。 */
    int insert(ProjectPhase phase);

    /** 修改阶段。 */
    int update(ProjectPhase phase);

    /** 删除阶段。 */
    int deleteById(Long id);

    /** 统计阶段下任务数量。 */
    int countTasks(Long id);

    /** 统计阶段下未完成的末级任务数量。 */
    int countIncompleteLeafTasks(Long id);

    /** 更新阶段生命周期状态。 */
    int updateLifecycle(ProjectPhase phase);

    /** 记录阶段生命周期操作日志。 */
    int insertLifecycleLog(@Param("phaseId") Long phaseId, @Param("projectId") Long projectId,
            @Param("action") String action, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("operator") String operator);

    /** 统计项目未完成阶段数量。 */
    int countIncompleteByProject(Long projectId);

    /** 统计项目阶段总数。 */
    int countByProject(Long projectId);
}
