package com.ruoyi.projectmanagement.project.service;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import java.util.List;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
/** 项目主档业务接口。 */
public interface IProjectInfoService {
    List<ProjectInfo> selectProjectInfoList(ProjectInfo project);
    ProjectInfo selectProjectInfoById(Long projectId);
    boolean checkProjectCodeUnique(ProjectInfo project);
    int insertProjectInfo(ProjectInfo project);
    int updateProjectInfo(ProjectInfo project);
    int deleteProjectInfoByIds(Long[] projectIds);
    int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator);
    List<ProjectPreliminaryPlan> preliminaryPlans(Long projectId); int addPreliminaryPlan(ProjectPreliminaryPlan plan,String operator); int updatePreliminaryPlan(ProjectPreliminaryPlan plan,String operator); int deletePreliminaryPlan(Long planId,String operator);
    int submitInitiation(Long projectId,String operator); int reviewInitiation(Long projectId,InitiationReviewRequest request,String operator); List<ProjectInitiationApproval> approvalHistory(Long projectId); ProjectInitiationApproval approvalSnapshot(Long projectId,Long approvalId);
}
