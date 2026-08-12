package com.ruoyi.projectmanagement.project.mapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/** 项目主档数据访问。 */
@Mapper
public interface ProjectInfoMapper {
    List<ProjectInfo> selectProjectInfoList(ProjectInfo project);
    ProjectInfo selectProjectInfoById(Long projectId);
    ProjectInfo selectProjectInfoByCode(String projectCode);
    int insertProjectInfo(ProjectInfo project);
    int updateProjectInfo(ProjectInfo project);
    int deleteProjectInfoByIds(Long[] projectIds);
    int updateLifecycle(ProjectInfo project);
    int countIncompleteTasksByProjectId(@Param("projectId") Long projectId);
    int insertLifecycleLog(@Param("projectId") Long projectId, @Param("action") String action, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus, @Param("reason") String reason, @Param("operator") String operator);
}
