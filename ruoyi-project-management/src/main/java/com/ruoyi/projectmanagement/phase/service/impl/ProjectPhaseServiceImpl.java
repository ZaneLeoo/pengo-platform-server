package com.ruoyi.projectmanagement.phase.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.common.enums.ProjectPhaseStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import com.ruoyi.projectmanagement.phase.mapper.ProjectPhaseMapper;
import com.ruoyi.projectmanagement.phase.service.IProjectPhaseService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 项目阶段业务实现。
 */
@Service
public class ProjectPhaseServiceImpl implements IProjectPhaseService {

    private final ProjectPhaseMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final IProjectTeamService teamService;

    public ProjectPhaseServiceImpl(ProjectPhaseMapper mapper, ProjectInfoMapper projectMapper,
            IProjectTeamService teamService) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.teamService = teamService;
    }

    @Override
    public List<ProjectPhase> list(ProjectPhase phase) {
        return mapper.selectList(phase);
    }

    @Override
    public ProjectPhase get(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int add(ProjectPhase phase, String op) {
        assertPlanningAllowed(phase.getProjectId());
        phase.setStatus(ProjectPhaseStatus.NOT_STARTED.getCode());
        validate(phase);
        phase.setCreateBy(op);
        return mapper.insert(phase);
    }

    @Override
    public int edit(ProjectPhase phase, String op) {
        ProjectPhase old = required(phase.getPhaseId());
        assertOwner(old, op);
        assertPlanningAllowed(old.getProjectId());
        if (ProjectPhaseStatus.COMPLETED.matches(old.getStatus())) {
            throw new ServiceException("已完成阶段不能编辑");
        }
        phase.setProjectId(old.getProjectId());
        phase.setStatus(old.getStatus());
        validate(phase);
        phase.setUpdateBy(op);
        return mapper.update(phase);
    }

    @Override
    public int remove(Long id, String op) {
        ProjectPhase phase = required(id);
        assertOwner(phase, op);
        assertPlanningAllowed(phase.getProjectId());
        if (mapper.countTasks(id) > 0) {
            throw new ServiceException("阶段下已有WBS任务，不能删除");
        }
        if (ProjectPhaseStatus.COMPLETED.matches(phase.getStatus())) {
            throw new ServiceException("已完成阶段不能删除");
        }
        return mapper.deleteById(id);
    }

    @Override
    public int lifecycle(Long id, String action, String op) {
        ProjectPhase phase = required(id);
        assertOwner(phase, op);
        ProjectInfo project = projectMapper.selectProjectInfoById(phase.getProjectId());
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        String from = phase.getStatus();
        String a = action.trim().toUpperCase();
        String to;
        if ("START".equals(a)) {
            if (!ProjectPhaseStatus.NOT_STARTED.matches(from)) {
                throw new ServiceException("只有未开始阶段可以开始");
            }
            if (!ProjectStatus.ACTIVE.matches(project.getStatus())) {
                throw new ServiceException("项目未执行中，不能开始阶段");
            }
            to = ProjectPhaseStatus.ACTIVE.getCode();
            phase.setActualStartDate(LocalDate.now());
        } else if ("COMPLETE".equals(a)) {
            if (!ProjectPhaseStatus.ACTIVE.matches(from)) {
                throw new ServiceException("只有执行中的阶段可以完成");
            }
            if (mapper.countTasks(id) == 0) {
                throw new ServiceException("空阶段不能完成，请先添加WBS任务");
            }
            int n = mapper.countIncompleteLeafTasks(id);
            if (n > 0) {
                throw new ServiceException("阶段仍有" + n + "个末级WBS任务未完成");
            }
            to = ProjectPhaseStatus.COMPLETED.getCode();
            phase.setActualEndDate(LocalDate.now());
        } else {
            throw new ServiceException("不支持的阶段动作");
        }
        phase.setStatus(to);
        phase.setUpdateBy(op);
        int rows = mapper.updateLifecycle(phase);
        if (rows > 0) {
            mapper.insertLifecycleLog(id, phase.getProjectId(), a, from, to, op);
        }
        return rows;
    }

    @Override
    public boolean allCompleted(Long projectId) {
        return mapper.countByProject(projectId) > 0 && mapper.countIncompleteByProject(projectId) == 0;
    }

    private ProjectPhase required(Long id) {
        ProjectPhase phase = mapper.selectById(id);
        if (phase == null) {
            throw new ServiceException("项目阶段不存在");
        }
        return phase;
    }

    private void validate(ProjectPhase phase) {
        ProjectInfo project = projectMapper.selectProjectInfoById(phase.getProjectId());
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (phase.getOwnerId() == null) {
            throw new ServiceException("请选择阶段负责人");
        }
        if (!teamService.isActiveMember(phase.getProjectId(), phase.getOwnerId())) {
            throw new ServiceException("阶段负责人必须是当前项目的在组成员");
        }
        if (phase.getStartDate() == null || phase.getEndDate() == null) {
            throw new ServiceException("请完整填写阶段计划开始和结束日期");
        }
        if (phase.getEndDate().isBefore(phase.getStartDate())) {
            throw new ServiceException("阶段结束日期不能早于开始日期");
        }
        if (phase.getStartDate().isBefore(project.getStartDate())
                || phase.getEndDate().isAfter(project.getEndDate())) {
            throw new ServiceException("阶段计划日期必须在项目计划周期内");
        }
        if (phase.getSortOrder() == null) {
            phase.setSortOrder(0);
        }
    }

    private void assertOwner(ProjectPhase phase, String op) {
        if (!"admin".equalsIgnoreCase(op)
                && (StringUtils.isBlank(phase.getOwnerCode()) || !phase.getOwnerCode().equalsIgnoreCase(op))) {
            throw new ServiceException("只有阶段负责人或admin可以执行该操作");
        }
    }

    private void assertPlanningAllowed(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (ProjectStatus.DRAFT.matches(project.getStatus())
                || ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            throw new ServiceException("项目尚未正式立项，不能维护项目计划");
        }
    }
}
