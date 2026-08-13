package com.ruoyi.projectmanagement.team.mapper;

import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.domain.ProjectRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目团队数据访问接口。
 */
@Mapper
public interface ProjectTeamMapper {

    /** 查询项目成员列表。 */
    List<ProjectMember> selectMembers(ProjectMember member);

    /** 根据成员ID查询成员。 */
    ProjectMember selectMemberById(Long id);

    /** 查询项目中的活跃成员。 */
    ProjectMember selectActiveMember(@Param("projectId") Long projectId, @Param("personId") Long personId);

    /** 新增项目成员。 */
    int insertMember(ProjectMember member);

    /** 更新项目成员。 */
    int updateMember(ProjectMember member);

    /** 成员退出（记录退出日期）。 */
    int exitMember(ProjectMember member);

    /** 统计项目活跃成员数。 */
    int countActiveMembers(Long projectId);

    /** 统计成员未完成任务数。 */
    int countIncompleteTasks(@Param("projectId") Long projectId, @Param("personId") Long personId);

    /** 查询项目角色列表。 */
    List<ProjectRole> selectRoles(Long projectId);

    /** 根据角色ID查询角色。 */
    ProjectRole selectRoleById(Long id);

    /** 查询系统预置角色。 */
    ProjectRole selectSystemRole(String code);

    /** 新增项目角色。 */
    int insertRole(ProjectRole role);

    /** 更新项目角色。 */
    int updateRole(ProjectRole role);

    /** 统计角色在组成员数。 */
    int countRoleMembers(Long roleId);
}
