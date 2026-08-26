package com.ruoyi.projectmanagement.team.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.common.enums.ProjectMemberStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.TeamRoleCode;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import com.ruoyi.projectmanagement.professionalrole.mapper.ProfessionalRoleMapper;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.domain.ProjectRole;
import com.ruoyi.projectmanagement.team.mapper.ProjectTeamMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 项目团队业务实现。 */
@Service
public class ProjectTeamServiceImpl implements IProjectTeamService {

    private final ProjectTeamMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectPersonMapper personMapper;
    private final ProfessionalRoleMapper professionalRoleMapper;

    public ProjectTeamServiceImpl(
            ProjectTeamMapper mapper,
            ProjectInfoMapper projectMapper,
            ProjectPersonMapper personMapper,
            ProfessionalRoleMapper professionalRoleMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.personMapper = personMapper;
        this.professionalRoleMapper = professionalRoleMapper;
    }

    @Override
    public List<ProjectMember> members(ProjectMember filter) {
        return mapper.selectMembers(filter);
    }

    @Override
    public List<ProjectRole> roles(Long projectId) {
        return mapper.selectRoles(projectId);
    }

    @Override
    public boolean isActiveMember(Long projectId, Long personId) {
        return mapper.selectActiveMember(projectId, personId) != null;
    }

    @Override
    public int activeCount(Long projectId) {
        return mapper.countActiveMembers(projectId);
    }

    @Override
    @Transactional
    public void addMembers(Long projectId, List<ProjectMember> members, String operator) {
        assertProjectExists(projectId);
        assertMutable(projectId);
        ProjectRole defaultRole = mapper.selectSystemRole(TeamRoleCode.MEMBER.getCode());
        for (ProjectMember member : members) {
            if (member.getPersonId() == null) {
                throw new ServiceException("请选择人员");
            }
            if (personMapper.selectProjectPersonById(member.getPersonId()) == null) {
                throw new ServiceException("人员档案不存在");
            }
            if (mapper.selectActiveMember(projectId, member.getPersonId()) != null) {
                throw new ServiceException("人员已在项目团队中，不能重复添加");
            }
            member.setProjectId(projectId);
            if (member.getRoleId() == null) {
                member.setRoleId(defaultRole.getRoleId());
            }
            validateRole(projectId, member.getRoleId());
            validateProfessionalRole(member);
            if (member.getJoinDate() == null) {
                member.setJoinDate(LocalDate.now());
            }
            member.setStatus(ProjectMemberStatus.ACTIVE.getCode());
            member.setCreateBy(operator);
            mapper.insertMember(member);
        }
    }

    @Override
    public int updateMember(ProjectMember member, String operator) {
        ProjectMember old = required(member.getMemberId());
        assertProjectExists(old.getProjectId());
        assertMutable(old.getProjectId());
        if (TeamRoleCode.PROJECT_MANAGER.matches(old.getRoleCode())) {
            throw new ServiceException("项目负责人请通过变更项目负责人调整");
        }
        validateRole(old.getProjectId(), member.getRoleId());
        validateProfessionalRole(member);
        member.setUpdateBy(operator);
        return mapper.updateMember(member);
    }

    @Override
    public int exitMember(Long id, String operator) {
        ProjectMember member = required(id);
        assertProjectExists(member.getProjectId());
        assertMutable(member.getProjectId());
        if (TeamRoleCode.PROJECT_MANAGER.matches(member.getRoleCode())) {
            throw new ServiceException("项目负责人不能退出团队，请先变更项目负责人");
        }
        int count = mapper.countIncompleteTasks(member.getProjectId(), member.getPersonId());
        if (count > 0) {
            throw new ServiceException("该成员仍负责" + count + "项未完成任务，请先转交任务");
        }
        member.setExitDate(LocalDate.now());
        member.setUpdateBy(operator);
        return mapper.exitMember(member);
    }

    @Override
    public int addRole(ProjectRole role, String operator) {
        assertProjectExists(role.getProjectId());
        assertMutable(role.getProjectId());
        role.setRoleCode("CUSTOM_" + System.currentTimeMillis());
        role.setSystemFlag("0");
        role.setStatus("0");
        role.setCreateBy(operator);
        return mapper.insertRole(role);
    }

    @Override
    public int updateRole(ProjectRole role, String operator) {
        ProjectRole old = mapper.selectRoleById(role.getRoleId());
        if (old == null || "1".equals(old.getSystemFlag())) {
            throw new ServiceException("系统预置角色不可修改");
        }
        assertProjectExists(old.getProjectId());
        assertMutable(old.getProjectId());
        if ("1".equals(role.getStatus()) && mapper.countRoleMembers(role.getRoleId()) > 0) {
            throw new ServiceException("该角色仍有在组成员，不能停用");
        }
        role.setUpdateBy(operator);
        return mapper.updateRole(role);
    }

    @Override
    @Transactional
    public void ensureManager(
            Long projectId, Long managerId, Long previousManagerId, String operator) {
        ProjectRole manager = mapper.selectSystemRole(TeamRoleCode.PROJECT_MANAGER.getCode());
        ProjectRole core = mapper.selectSystemRole(TeamRoleCode.CORE_MEMBER.getCode());
        // 原负责人降为核心成员
        if (previousManagerId != null && !previousManagerId.equals(managerId)) {
            ProjectMember old = mapper.selectActiveMember(projectId, previousManagerId);
            if (old != null) {
                old.setRoleId(core.getRoleId());
                old.setUpdateBy(operator);
                mapper.updateMember(old);
            }
        }
        // 新负责人加入团队
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setPersonId(managerId);
        member.setRoleId(manager.getRoleId());
        ProfessionalRole professionalRole = professionalRoleMapper.selectByCode("PROJECT_MANAGER");
        if (professionalRole == null || !"0".equals(professionalRole.getStatus())) {
            throw new ServiceException("系统未配置启用的项目经理专业角色");
        }
        member.setProfessionalRoleId(professionalRole.getProfessionalRoleId());
        member.setSpecialtyRole(professionalRole.getRoleName());
        member.setResponsibility("负责项目总体目标、计划与协调");
        member.setJoinDate(LocalDate.now());
        member.setStatus(ProjectMemberStatus.ACTIVE.getCode());
        member.setCreateBy(operator);
        mapper.insertMember(member);
    }

    private ProjectMember required(Long id) {
        ProjectMember member = mapper.selectMemberById(id);
        if (member == null) {
            throw new ServiceException("项目成员不存在");
        }
        return member;
    }

    private void validateRole(Long projectId, Long roleId) {
        ProjectRole role = mapper.selectRoleById(roleId);
        if (role == null
                || (!Long.valueOf(0).equals(role.getProjectId())
                        && !projectId.equals(role.getProjectId()))) {
            throw new ServiceException("项目角色不可用");
        }
    }

    /** 校验并回填全局专业角色，同时保留成员记录中的名称快照。 */
    private void validateProfessionalRole(ProjectMember member) {
        if (member.getProfessionalRoleId() == null) {
            throw new ServiceException("请选择专业角色");
        }
        ProfessionalRole role = professionalRoleMapper.selectById(member.getProfessionalRoleId());
        if (role == null || !"0".equals(role.getStatus())) {
            throw new ServiceException("专业角色不存在或已停用");
        }
        member.setProfessionalRoleCode(role.getRoleCode());
        member.setProfessionalRoleName(role.getRoleName());
        member.setSpecialtyRole(role.getRoleName());
    }

    private void assertMutable(Long id) {
        String status = projectMapper.selectProjectInfoById(id).getStatus();
        if (!ProjectStatus.DRAFT.matches(status) && !ProjectStatus.APPROVED.matches(status)) {
            throw new ServiceException("仅项目草稿或已立项待启动阶段可以维护团队");
        }
    }

    /** 校验项目存在且允许调整团队。 */
    private void assertProjectExists(Long projectId) {
        if (projectMapper.selectProjectInfoById(projectId) == null) {
            throw new ServiceException("项目不存在");
        }
    }
}
