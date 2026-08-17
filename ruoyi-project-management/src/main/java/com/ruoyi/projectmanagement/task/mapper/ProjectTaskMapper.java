package com.ruoyi.projectmanagement.task.mapper;

import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOutput;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOperationLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectTaskMapper {
    List<ProjectTask> selectList(ProjectTask filter);
    ProjectTask selectById(Long id);
    List<ProjectTask> selectChildren(@Param("packageId") Long packageId, @Param("parentId") Long parentId);
    int insert(ProjectTask task);
    int update(ProjectTask task);
    int delete(Long id);
    int countChildren(Long id);
    int updateLifecycle(ProjectTask task);
    int updateAggregate(@Param("id") Long id, @Param("status") String status,
            @Param("progress") Integer progress);
    List<ProjectTaskOutput> selectOutputs(Long taskId);
    ProjectTaskOutput selectOutput(Long id);
    int insertOutput(ProjectTaskOutput output);
    int deleteOutput(Long id);
    List<ProjectTaskOperationLog> selectOperationLogs(Long taskId);
    int insertOperationLog(ProjectTaskOperationLog log);
}
