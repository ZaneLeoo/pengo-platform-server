package com.ruoyi.projectmanagement.project.mapper;

import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationApproval;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目主档数据访问接口。
 */
@Mapper
public interface ProjectInfoMapper {

    /** 查询项目列表。 */
    List<ProjectInfo> selectProjectInfoList(ProjectInfo project);

    /** 根据项目ID查询项目。 */
    ProjectInfo selectProjectInfoById(Long projectId);

    /** 根据项目编码查询项目。 */
    ProjectInfo selectProjectInfoByCode(String projectCode);

    /** 新增项目。 */
    int insertProjectInfo(ProjectInfo project);

    /** 修改项目基本信息。 */
    int updateProjectInfo(ProjectInfo project);

    /** 批量删除项目。 */
    int deleteProjectInfoByIds(Long[] projectIds);

    /** 更新项目生命周期状态。 */
    int updateLifecycle(ProjectInfo project);

    /** 记录项目生命周期操作日志。 */
    int insertLifecycleLog(@Param("projectId") Long projectId, @Param("action") String action,
            @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus,
            @Param("reason") String reason, @Param("operator") String operator);

    /** 查询项目初步计划列表。 */
    List<ProjectPreliminaryPlan> selectPreliminaryPlans(Long projectId);

    /** 根据计划ID查询初步计划。 */
    ProjectPreliminaryPlan selectPreliminaryPlan(Long planId);

    /** 新增初步计划。 */
    int insertPreliminaryPlan(ProjectPreliminaryPlan plan);

    /** 修改初步计划。 */
    int updatePreliminaryPlan(ProjectPreliminaryPlan plan);

    /** 删除初步计划。 */
    int deletePreliminaryPlan(Long planId);

    /** 新增立项审批记录。 */
    int insertApproval(ProjectInitiationApproval approval);

    /** 查询立项审批历史。 */
    List<ProjectInitiationApproval> selectApprovals(Long projectId);

    /** 查询指定立项审批快照。 */
    ProjectInitiationApproval selectApprovalById(@Param("projectId") Long projectId,
            @Param("approvalId") Long approvalId);

    /** 查询待审批的立项记录。 */
    ProjectInitiationApproval selectPendingApproval(Long projectId);

    /** 更新立项审批结果。 */
    int reviewApproval(ProjectInitiationApproval approval);

    /** 更新项目立项状态。 */
    int updateInitiationState(ProjectInfo project);

    /** 标记WBS概要已转为顶层WBS。 */
    int markPlanConverted(@Param("planId") Long planId, @Param("wbsId") Long wbsId);
}
