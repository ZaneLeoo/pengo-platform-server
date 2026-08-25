package com.ruoyi.projectmanagement.issue.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.common.enums.IssueSeverity;
import com.ruoyi.projectmanagement.common.enums.IssueStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.mapper.ProjectIssueMapper;
import com.ruoyi.projectmanagement.issue.service.IProjectIssueService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 问题跟踪业务实现。
 */
@Service
public class ProjectIssueServiceImpl implements IProjectIssueService {

    private final ProjectIssueMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectTaskMapper taskMapper;
    private final IProjectTeamService teamService;

    public ProjectIssueServiceImpl(ProjectIssueMapper mapper, ProjectInfoMapper projectMapper,
            ProjectWbsMapper wbsMapper, ProjectTaskMapper taskMapper, IProjectTeamService teamService) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.wbsMapper = wbsMapper;
        this.taskMapper = taskMapper;
        this.teamService = teamService;
    }

    /** 查询问题列表。 */
    @Override
    public List<ProjectIssue> list(ProjectIssue filter) {
        return mapper.selectList(filter);
    }

    /** 查询问题详细，不存在时抛异常。 */
    @Override
    public ProjectIssue get(Long id) {
        ProjectIssue issue = mapper.selectById(id);
        if (issue == null) {
            throw new ServiceException("问题不存在");
        }
        return issue;
    }

    /** 新增问题，自动生成编码并填充默认严重程度与状态。 */
    @Override
    public int add(ProjectIssue issue, String operator) {
        validate(issue);
        assertMutable(issue.getProjectId());
        issue.setIssueCode(nextCode(issue.getProjectId()));
        if (StringUtils.isBlank(issue.getSeverity())) {
            issue.setSeverity(IssueSeverity.MEDIUM.getCode());
        }
        if (StringUtils.isBlank(issue.getStatus())) {
            issue.setStatus(IssueStatus.OPEN.getCode());
        }
        issue.setCreateBy(operator);
        return mapper.insert(issue);
    }

    /** 修改问题，项目与编码不允许变更。 */
    @Override
    public int edit(ProjectIssue issue, String operator) {
        ProjectIssue old = get(issue.getIssueId());
        issue.setProjectId(old.getProjectId());
        issue.setIssueCode(old.getIssueCode());
        validate(issue);
        assertMutable(issue.getProjectId());
        issue.setUpdateBy(operator);
        return mapper.update(issue);
    }

    /** 删除问题，先确认记录存在。 */
    @Override
    public int remove(Long[] ids, String operator) {
        for (Long id : ids) {
            ProjectIssue issue = get(id);
            assertMutable(issue.getProjectId());
        }
        return mapper.delete(ids);
    }

    /** 按项目内序号生成问题编码，如 ISS-001。 */
    private String nextCode(Long projectId) {
        return "ISS-" + String.format("%03d", mapper.countByProject(projectId) + 1);
    }

    /**
     * 校验问题关联关系：项目、工作包、任务与负责人都必须属于当前项目。
     */
    private void validate(ProjectIssue issue) {
        ProjectInfo project = projectMapper.selectProjectInfoById(issue.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        ProjectWbsNode workPackage = null;
        if (issue.getWorkPackageId() != null) {
            workPackage = wbsMapper.selectById(issue.getWorkPackageId());
            if (workPackage == null || !issue.getProjectId().equals(workPackage.getProjectId())) {
                throw new ServiceException("关联工作包不属于当前项目");
            }
        }
        if (issue.getTaskId() != null) {
            ProjectTask task = taskMapper.selectById(issue.getTaskId());
            if (task == null || !issue.getProjectId().equals(task.getProjectId())) {
                throw new ServiceException("关联任务不属于当前项目");
            }
            if (workPackage != null && !workPackage.getWbsId().equals(task.getWorkPackageId())) {
                throw new ServiceException("关联任务不属于所选工作包");
            }
            issue.setWorkPackageId(task.getWorkPackageId());
        }
        if (issue.getOwnerId() != null && !teamService.isActiveMember(issue.getProjectId(), issue.getOwnerId())) {
            throw new ServiceException("问题负责人必须是当前项目在组成员");
        }
    }

    /** 问题跟踪仅在项目执行中开放维护。 */
    private void assertMutable(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (!ProjectStatus.ACTIVE.matches(project.getStatus())) {
            throw new ServiceException("仅执行中的项目允许维护问题");
        }
    }
}
