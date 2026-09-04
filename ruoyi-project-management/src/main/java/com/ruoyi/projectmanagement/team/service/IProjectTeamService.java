package com.ruoyi.projectmanagement.team.service;

import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.domain.ProjectRole;
import java.util.List;

/** 项目团队服务接口。 */
public interface IProjectTeamService {

    /** 查询项目成员列表。 */
    List<ProjectMember> members(ProjectMember filter);

    /** 批量添加项目成员。 */
    void addMembers(
            Long projectId, List<ProjectMember> members, String operator, Long operatorUserId);

    /** 更新项目成员（角色、职责等）。 */
    int updateMember(ProjectMember member, String operator, Long operatorUserId);

    /** 成员退出项目团队。 */
    int exitMember(Long memberId, String operator, Long operatorUserId);

    /** 查询项目角色列表。 */
    List<ProjectRole> roles(Long projectId);

    /** 新增自定义项目角色。 */
    int addRole(ProjectRole role, String operator, Long operatorUserId);

    /** 更新自定义项目角色。 */
    int updateRole(ProjectRole role, String operator, Long operatorUserId);

    /** 判断人员是否为项目活跃成员。 */
    boolean isActiveMember(Long projectId, Long personId);

    /** 统计项目活跃成员数。 */
    int activeCount(Long projectId);

    /** 确保项目负责人作为成员存在，原负责人降为核心成员。 */
    void ensureManager(Long projectId, Long managerId, Long previousManagerId, String operator);
}
