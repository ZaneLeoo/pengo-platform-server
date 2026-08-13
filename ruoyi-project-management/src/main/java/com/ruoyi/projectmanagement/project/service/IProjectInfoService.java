package com.ruoyi.projectmanagement.project.service;

import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import java.util.List;
import java.util.Map;

/**
 * 项目主档业务接口。
 */
public interface IProjectInfoService {

    /** 查询项目列表。 */
    List<ProjectInfo> selectProjectInfoList(ProjectInfo project);

    /** 根据项目ID查询项目。 */
    ProjectInfo selectProjectInfoById(Long projectId);

    /** 校验项目编码是否唯一。 */
    boolean checkProjectCodeUnique(ProjectInfo project);

    /** 新增项目。 */
    int insertProjectInfo(ProjectInfo project);

    /** 修改项目基本信息。 */
    int updateProjectInfo(ProjectInfo project);

    /** 批量删除项目。 */
    int deleteProjectInfoByIds(Long[] projectIds);

    /** 执行项目生命周期动作（启动/暂停/恢复/完成）。 */
    int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator);

    /** 查询项目初步计划列表。 */
    List<ProjectPreliminaryPlan> preliminaryPlans(Long projectId);

    /** 新增初步计划。 */
    int addPreliminaryPlan(ProjectPreliminaryPlan plan, String operator);

    /** 修改初步计划。 */
    int updatePreliminaryPlan(ProjectPreliminaryPlan plan, String operator);

    /** 删除初步计划。 */
    int deletePreliminaryPlan(Long planId, String operator);

    /** 提交立项申请。 */
    int submitInitiation(Long projectId, String operator);

    /** 审批立项申请。 */
    int reviewInitiation(Long projectId, InitiationReviewRequest request, String operator);

    /** 查询立项审批历史。 */
    List<ProjectInitiationApproval> approvalHistory(Long projectId);

    /** 查询立项审批快照。 */
    ProjectInitiationApproval approvalSnapshot(Long projectId, Long approvalId);

    /** 项目启动前就绪检查。 */
    Map<String, Object> startReadiness(Long projectId);
}
