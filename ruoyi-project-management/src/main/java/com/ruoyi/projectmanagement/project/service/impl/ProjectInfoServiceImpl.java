package com.ruoyi.projectmanagement.project.service.impl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.phase.service.IProjectPhaseService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** 项目主档业务实现。 */
@Service
public class ProjectInfoServiceImpl implements IProjectInfoService {
    private final ProjectInfoMapper projectMapper;
    private final ProjectCategoryMapper categoryMapper;
    private final ProjectPersonMapper personMapper;
    private final IProjectTeamService teamService;
    private final IProjectPhaseService phaseService;
    public ProjectInfoServiceImpl(ProjectInfoMapper p, ProjectCategoryMapper c, ProjectPersonMapper m, IProjectTeamService teamService, IProjectPhaseService phaseService) {
        projectMapper = p;
        categoryMapper = c;
        personMapper = m;
        this.teamService = teamService;
        this.phaseService = phaseService;
    }
    @Override
    public List<ProjectInfo> selectProjectInfoList(ProjectInfo project) {
        return projectMapper.selectProjectInfoList(project);
    }
    @Override
    public ProjectInfo selectProjectInfoById(Long id) {
        return projectMapper.selectProjectInfoById(id);
    }
    @Override
    public boolean checkProjectCodeUnique(ProjectInfo p) {
        Long id = StringUtils.isNull(p.getProjectId()) ? -1L : p.getProjectId();
        ProjectInfo e = projectMapper.selectProjectInfoByCode(p.getProjectCode());
        return e == null || e.getProjectId().longValue() == id.longValue();
    }
    @Override
    @Transactional
    public int insertProjectInfo(ProjectInfo p) {
        p.setStatus("DRAFT");
        p.setProgress(0);
        validate(p);
        int rows = projectMapper.insertProjectInfo(p);
        if (rows > 0) teamService.ensureManager(p.getProjectId(), p.getManagerId(), null, p.getCreateBy());
        return rows;
    }
    @Override
    @Transactional
    public int updateProjectInfo(ProjectInfo p) {
        ProjectInfo existing = projectMapper.selectProjectInfoById(p.getProjectId());
        if (existing == null) throw new ServiceException("项目不存在");
        p.setStatus(existing.getStatus());
        p.setActualStartDate(existing.getActualStartDate());
        p.setActualEndDate(existing.getActualEndDate());
        p.setPauseReason(existing.getPauseReason());
        validate(p);
        int rows = projectMapper.updateProjectInfo(p);
        if (rows > 0 && !existing.getManagerId().equals(p.getManagerId())) teamService.ensureManager(p.getProjectId(), p.getManagerId(), existing.getManagerId(), p.getUpdateBy());
        return rows;
    }
    @Override
    public int deleteProjectInfoByIds(Long[] ids) {
        return projectMapper.deleteProjectInfoByIds(ids);
    }
    @Override
    public int applyLifecycleAction(Long projectId, LifecycleActionRequest request, String operator) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) throw new ServiceException("项目不存在");
        assertOperator(project.getManagerCode(), operator, "只有项目负责人或admin可以执行该项目动作");
        String from = project.getStatus();
        String action = request.getAction().trim().toUpperCase();
        String to;
        switch (action) {
            case "START" -> {
                if (!"DRAFT".equals(from) && !"PLANNED".equals(from)) throw new ServiceException("只有未启动项目可以启动");
                if (teamService.activeCount(projectId) == 0) throw new ServiceException("项目团队尚未组建，不能启动项目");
                to = "ACTIVE";
                project.setActualStartDate(LocalDate.now());
            }
            case "PAUSE" -> {
                if (!"ACTIVE".equals(from)) throw new ServiceException("只有执行中的项目可以暂停");
                if (StringUtils.isBlank(request.getReason())) throw new ServiceException("暂停项目必须填写原因");
                to = "PAUSED";
                project.setPauseReason(request.getReason().trim());
            }
            case "RESUME" -> {
                if (!"PAUSED".equals(from)) throw new ServiceException("只有已暂停项目可以恢复");
                to = "ACTIVE";
                project.setPauseReason(null);
            }
            case "COMPLETE" -> {
                if (!"ACTIVE".equals(from)) throw new ServiceException("只有执行中的项目可以完成");
                if (!phaseService.allCompleted(projectId)) throw new ServiceException("请先完成项目的全部阶段");
                to = "COMPLETED";
                project.setActualEndDate(LocalDate.now());
                project.setProgress(100);
            }
            default -> throw new ServiceException("不支持的项目生命周期动作");
        }
        project.setStatus(to);
        project.setUpdateBy(operator);
        int rows = projectMapper.updateLifecycle(project);
        if (rows > 0) projectMapper.insertLifecycleLog(projectId, action, from, to, request.getReason(), operator);
        return rows;
    }
    private void validate(ProjectInfo p) {
        if (p.getEndDate().isBefore(p.getStartDate()))
            throw new ServiceException("计划结束日期不能早于开始日期");
        if (categoryMapper.selectProjectCategoryById(p.getCategoryId()) == null)
            throw new ServiceException("项目分类不存在");
        var manager = personMapper.selectProjectPersonById(p.getManagerId());
        if (manager == null || !"0".equals(manager.getStatus()))
            throw new ServiceException("请选择启用状态的项目负责人");
        if (p.getProgress() == null)
            p.setProgress(0);
        if (StringUtils.isBlank(p.getStatus()))
            p.setStatus("DRAFT");
    }
    private void assertOperator(String ownerCode, String operator, String message) {
        if (!"admin".equalsIgnoreCase(operator) && (StringUtils.isBlank(ownerCode) || !ownerCode.equalsIgnoreCase(operator))) throw new ServiceException(message);
    }
}
