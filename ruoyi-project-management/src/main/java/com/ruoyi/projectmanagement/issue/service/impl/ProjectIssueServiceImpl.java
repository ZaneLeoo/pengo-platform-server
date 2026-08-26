package com.ruoyi.projectmanagement.issue.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.common.enums.IssueSeverity;
import com.ruoyi.projectmanagement.common.enums.IssueStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.issue.domain.IssueTransitionRequest;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueActivity;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueCapability;
import com.ruoyi.projectmanagement.issue.mapper.ProjectIssueMapper;
import com.ruoyi.projectmanagement.issue.service.IProjectIssueService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问题跟踪业务实现。 */
@Service
public class ProjectIssueServiceImpl implements IProjectIssueService {

    private final ProjectIssueMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWbsMapper wbsMapper;
    private final ProjectTaskMapper taskMapper;
    private final IProjectTeamService teamService;

    public ProjectIssueServiceImpl(
            ProjectIssueMapper mapper,
            ProjectInfoMapper projectMapper,
            ProjectWbsMapper wbsMapper,
            ProjectTaskMapper taskMapper,
            IProjectTeamService teamService) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.wbsMapper = wbsMapper;
        this.taskMapper = taskMapper;
        this.teamService = teamService;
    }

    /** 查询问题列表。 */
    @Override
    public List<ProjectIssue> list(ProjectIssue filter, Long userId) {
        filter.setViewerUserId(userId);
        filter.setViewerAdmin(SecurityUtils.isAdmin(userId));
        List<ProjectIssue> issues = mapper.selectList(filter);
        issues.forEach(issue -> enrichCapabilities(issue, userId));
        return issues;
    }

    /** 查询问题详细，不存在时抛异常。 */
    @Override
    public ProjectIssue get(Long id, Long userId) {
        ProjectIssue issue = requireIssue(id);
        assertViewable(issue, userId);
        enrichCapabilities(issue, userId);
        return issue;
    }

    @Override
    public ProjectIssueCapability capability(Long projectId, Long userId) {
        ProjectInfo project = requireProject(projectId);
        boolean mutable = isMutable(project);
        boolean member =
                SecurityUtils.isAdmin(userId)
                        || userId.equals(project.getManagerId())
                        || teamService.isActiveMember(projectId, userId);
        if (!mutable) {
            return new ProjectIssueCapability(false, "仅已立项、执行中或暂停的项目允许新增问题");
        }
        return new ProjectIssueCapability(member, member ? null : "仅项目在组成员可以新增问题");
    }

    private ProjectIssue requireIssue(Long id) {
        ProjectIssue issue = mapper.selectById(id);
        if (issue == null) {
            throw new ServiceException("问题不存在");
        }
        return issue;
    }

    /** 新增问题，自动生成编码并填充默认严重程度与状态。 */
    @Override
    @Transactional
    public int add(ProjectIssue issue, String operator, Long userId) {
        validate(issue);
        assertMutable(issue.getProjectId());
        ProjectInfo project = requireProject(issue.getProjectId());
        if (!SecurityUtils.isAdmin(userId)
                && !userId.equals(project.getManagerId())
                && !teamService.isActiveMember(issue.getProjectId(), userId)) {
            throw new ServiceException("仅项目在组成员可以新增问题");
        }
        issue.setIssueCode(nextCode(issue.getProjectId()));
        issue.setReporterUserId(userId);
        if (StringUtils.isBlank(issue.getSeverity())) {
            issue.setSeverity(IssueSeverity.MEDIUM.getCode());
        }
        issue.setStatus(IssueStatus.OPEN.getCode());
        issue.setCreateBy(operator);
        int rows = mapper.insert(issue);
        record(issue.getIssueId(), "CREATED", "提出问题", null, issue.getStatus(), operator, userId);
        return rows;
    }

    /** 修改问题，项目与编码不允许变更。 */
    @Override
    public int edit(ProjectIssue issue, String operator, Long userId) {
        ProjectIssue old = requireIssue(issue.getIssueId());
        if (!canEdit(old, userId)) {
            throw new ServiceException("仅提出人或项目负责人可以编辑问题");
        }
        issue.setProjectId(old.getProjectId());
        issue.setIssueCode(old.getIssueCode());
        issue.setStatus(old.getStatus());
        issue.setResolution(old.getResolution());
        validate(issue);
        assertMutable(issue.getProjectId());
        issue.setUpdateBy(operator);
        return mapper.update(issue);
    }

    /** 删除问题，先确认记录存在。 */
    @Override
    @Transactional
    public int remove(Long[] ids, String operator, Long userId) {
        for (Long id : ids) {
            ProjectIssue issue = requireIssue(id);
            assertMutable(issue.getProjectId());
            if (!canEdit(issue, userId)) {
                throw new ServiceException("仅提出人或项目负责人可以删除问题");
            }
            if (!IssueStatus.OPEN.matches(issue.getStatus())) {
                throw new ServiceException("问题开始处理后不允许删除");
            }
        }
        mapper.deleteActivities(ids);
        return mapper.delete(ids);
    }

    /** 按问题闭环规则流转状态，禁止通过普通编辑绕过状态机。 */
    @Override
    @Transactional
    public int transition(Long id, IssueTransitionRequest request, String operator, Long userId) {
        ProjectIssue issue = requireIssue(id);
        assertMutable(issue.getProjectId());
        IssueStatus target = parseStatus(request.getTargetStatus());
        authorizeTransition(issue, target, request, userId);
        String oldStatus = issue.getStatus();
        issue.setStatus(target.getCode());
        if (IssueStatus.RESOLVED == target) {
            issue.setResolution(request.getResolution());
        } else if (IssueStatus.PROCESSING == target && IssueStatus.RESOLVED.matches(oldStatus)) {
            issue.setResolution(null);
        }
        issue.setUpdateBy(operator);
        int rows = mapper.updateStatus(issue);
        String content =
                StringUtils.isNotBlank(request.getReason())
                        ? request.getReason()
                        : request.getResolution();
        record(id, "STATUS", content, oldStatus, target.getCode(), operator, userId);
        return rows;
    }

    /** 添加评论或附件动态。 */
    @Override
    public int addActivity(Long id, ProjectIssueActivity activity, String operator, Long userId) {
        ProjectIssue issue = requireIssue(id);
        assertMutable(issue.getProjectId());
        if (!canAddActivity(issue, userId)) {
            throw new ServiceException("仅项目在组成员可以补充问题动态");
        }
        if (StringUtils.isBlank(activity.getContent())
                && StringUtils.isBlank(activity.getAttachmentUrl())) {
            throw new ServiceException("评论内容和附件至少填写一项");
        }
        boolean attachment = StringUtils.isNotBlank(activity.getAttachmentUrl());
        activity.setContent(
                StringUtils.isBlank(activity.getContent()) ? "上传附件" : activity.getContent());
        activity.setIssueId(id);
        activity.setActivityType(attachment ? "ATTACHMENT" : "COMMENT");
        activity.setOperatorUserId(userId);
        activity.setCreateBy(operator);
        return mapper.insertActivity(activity);
    }

    /** 查询问题完整活动时间线。 */
    @Override
    public List<ProjectIssueActivity> activities(Long id, Long userId) {
        ProjectIssue issue = requireIssue(id);
        assertViewable(issue, userId);
        return mapper.selectActivities(id);
    }

    /** 按项目内序号生成问题编码，如 ISS-001。 */
    private String nextCode(Long projectId) {
        return "ISS-" + String.format("%03d", mapper.countByProject(projectId) + 1);
    }

    /** 校验问题关联关系：项目、工作包、任务与负责人都必须属于当前项目。 */
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
        if (issue.getOwnerId() != null
                && !teamService.isActiveMember(issue.getProjectId(), issue.getOwnerId())) {
            throw new ServiceException("问题负责人必须是当前项目在组成员");
        }
    }

    private IssueStatus parseStatus(String status) {
        try {
            return IssueStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("不支持的问题状态");
        }
    }

    private void authorizeTransition(
            ProjectIssue issue, IssueStatus target, IssueTransitionRequest request, Long userId) {
        if (SecurityUtils.isAdmin(userId) && isSupportedTransition(issue, target, request)) {
            return;
        }
        boolean managerOrOwner = isManager(issue, userId) || userId.equals(issue.getOwnerId());
        boolean reporterOrManager =
                isManager(issue, userId) || userId.equals(issue.getReporterUserId());
        if (IssueStatus.OPEN.matches(issue.getStatus())
                && target == IssueStatus.PROCESSING
                && managerOrOwner) {
            return;
        }
        if (IssueStatus.PROCESSING.matches(issue.getStatus())
                && target == IssueStatus.RESOLVED
                && managerOrOwner
                && StringUtils.isNotBlank(request.getResolution())) {
            return;
        }
        if (IssueStatus.RESOLVED.matches(issue.getStatus())
                && target == IssueStatus.CLOSED
                && reporterOrManager) {
            return;
        }
        if (IssueStatus.RESOLVED.matches(issue.getStatus())
                && target == IssueStatus.PROCESSING
                && reporterOrManager
                && StringUtils.isNotBlank(request.getReason())) {
            return;
        }
        throw new ServiceException("当前状态、操作人或必填说明不满足流转条件");
    }

    private boolean isManager(ProjectIssue issue, Long userId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(issue.getProjectId());
        return project != null && userId.equals(project.getManagerId());
    }

    private boolean isSupportedTransition(
            ProjectIssue issue, IssueStatus target, IssueTransitionRequest request) {
        return IssueStatus.OPEN.matches(issue.getStatus()) && target == IssueStatus.PROCESSING
                || IssueStatus.PROCESSING.matches(issue.getStatus())
                        && target == IssueStatus.RESOLVED
                        && StringUtils.isNotBlank(request.getResolution())
                || IssueStatus.RESOLVED.matches(issue.getStatus()) && target == IssueStatus.CLOSED
                || IssueStatus.RESOLVED.matches(issue.getStatus())
                        && target == IssueStatus.PROCESSING
                        && StringUtils.isNotBlank(request.getReason());
    }

    private void enrichCapabilities(ProjectIssue issue, Long userId) {
        boolean mutable = isMutable(requireProject(issue.getProjectId()));
        boolean edit = mutable && canEdit(issue, userId);
        issue.setCanEdit(edit);
        issue.setCanDelete(edit && IssueStatus.OPEN.matches(issue.getStatus()));
        issue.setCanAddActivity(mutable && canAddActivity(issue, userId));
        List<String> transitions = new ArrayList<>();
        if (!mutable) {
            issue.setAllowedTransitions(transitions);
            return;
        }
        boolean managerOrOwner =
                SecurityUtils.isAdmin(userId)
                        || isManager(issue, userId)
                        || userId.equals(issue.getOwnerId());
        boolean reporterOrManager =
                SecurityUtils.isAdmin(userId)
                        || isManager(issue, userId)
                        || userId.equals(issue.getReporterUserId());
        if (IssueStatus.OPEN.matches(issue.getStatus()) && managerOrOwner) {
            transitions.add(IssueStatus.PROCESSING.getCode());
        } else if (IssueStatus.PROCESSING.matches(issue.getStatus()) && managerOrOwner) {
            transitions.add(IssueStatus.RESOLVED.getCode());
        } else if (IssueStatus.RESOLVED.matches(issue.getStatus()) && reporterOrManager) {
            transitions.add(IssueStatus.CLOSED.getCode());
            transitions.add(IssueStatus.PROCESSING.getCode());
        }
        issue.setAllowedTransitions(transitions);
    }

    private boolean canEdit(ProjectIssue issue, Long userId) {
        return SecurityUtils.isAdmin(userId)
                || isManager(issue, userId)
                || userId.equals(issue.getReporterUserId());
    }

    private boolean canAddActivity(ProjectIssue issue, Long userId) {
        return SecurityUtils.isAdmin(userId)
                || isManager(issue, userId)
                || teamService.isActiveMember(issue.getProjectId(), userId);
    }

    private void assertViewable(ProjectIssue issue, Long userId) {
        if (!SecurityUtils.isAdmin(userId)
                && !isManager(issue, userId)
                && !teamService.isActiveMember(issue.getProjectId(), userId)
                && !userId.equals(issue.getReporterUserId())
                && !userId.equals(issue.getOwnerId())) {
            throw new ServiceException("您无权查看该问题");
        }
    }

    private ProjectInfo requireProject(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        return project;
    }

    private boolean isMutable(ProjectInfo project) {
        return ProjectStatus.APPROVED.matches(project.getStatus())
                || ProjectStatus.ACTIVE.matches(project.getStatus())
                || ProjectStatus.PAUSED.matches(project.getStatus());
    }

    private void record(
            Long issueId,
            String type,
            String content,
            String fromStatus,
            String toStatus,
            String operator,
            Long userId) {
        ProjectIssueActivity activity = new ProjectIssueActivity();
        activity.setIssueId(issueId);
        activity.setActivityType(type);
        activity.setContent(StringUtils.isBlank(content) ? "状态已更新" : content);
        activity.setFromStatus(fromStatus);
        activity.setToStatus(toStatus);
        activity.setOperatorUserId(userId);
        activity.setCreateBy(operator);
        mapper.insertActivity(activity);
    }

    /** 问题可在已立项、执行中和暂停项目维护；完成后只读。 */
    private void assertMutable(Long projectId) {
        ProjectInfo project = requireProject(projectId);
        if (!isMutable(project)) {
            throw new ServiceException("仅已立项、执行中或暂停的项目允许维护问题");
        }
    }
}
