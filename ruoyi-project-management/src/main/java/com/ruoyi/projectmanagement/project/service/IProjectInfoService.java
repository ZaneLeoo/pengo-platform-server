package com.ruoyi.projectmanagement.project.service;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import java.util.List;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
/** 项目主档业务接口。 */
public interface IProjectInfoService {
    List<ProjectInfo> selectProjectInfoList(ProjectInfo project);
    ProjectInfo selectProjectInfoById(Long projectId);
    boolean checkProjectCodeUnique(ProjectInfo project);
    int insertProjectInfo(ProjectInfo project);
    int updateProjectInfo(ProjectInfo project);
    int deleteProjectInfoByIds(Long[] projectIds);
    int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator);
}
