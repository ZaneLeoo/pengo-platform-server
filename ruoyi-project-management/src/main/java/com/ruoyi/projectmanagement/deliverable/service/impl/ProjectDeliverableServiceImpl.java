package com.ruoyi.projectmanagement.deliverable.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableService;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 项目交付物业务实现。
 */
@Service
public class ProjectDeliverableServiceImpl implements IProjectDeliverableService {

    private final ProjectDeliverableMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWorkItemMapper taskMapper;

    public ProjectDeliverableServiceImpl(ProjectDeliverableMapper mapper, ProjectInfoMapper projectMapper,
            ProjectWorkItemMapper taskMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<ProjectDeliverable> selectList(ProjectDeliverable entity) {
        return mapper.selectList(entity);
    }

    @Override
    public ProjectDeliverable selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int insert(ProjectDeliverable entity) {
        assertProjectAllowed(entity.getProjectId());
        prepare(entity);
        assertTaskEditable(entity.getTaskId());
        return mapper.insert(entity);
    }

    @Override
    public int update(ProjectDeliverable entity) {
        assertProjectAllowed(entity.getProjectId());
        prepare(entity);
        assertTaskEditable(entity.getTaskId());
        return mapper.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            ProjectDeliverable d = required(id);
            assertProjectAllowed(d.getProjectId());
            assertTaskEditable(d.getTaskId());
        }
        return mapper.deleteByIds(ids);
    }

    private void prepare(ProjectDeliverable entity) {
        if (projectMapper.selectProjectInfoById(entity.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
        }
        ProjectWorkItem task = taskMapper.selectById(entity.getTaskId());
        if (task == null || !"TASK".equals(task.getItemType())
                || !task.getProjectId().equals(entity.getProjectId())) {
            throw new ServiceException("关联WBS任务不存在或不属于当前项目");
        }
        if (entity.getRequiredFlag() == null) {
            entity.setRequiredFlag("1");
        }
        if (entity.getApprovalRequired() == null) {
            entity.setApprovalRequired("0");
        }
        entity.setReviewer("admin");
        if (entity.getStatus() == null) {
            entity.setStatus("PENDING");
        }
    }

    @Override
    public void submit(Long id, ProjectDeliverableSubmission submission, String username) {
        ProjectDeliverable d = required(id);
        assertProjectAllowed(d.getProjectId());
        ProjectWorkItem task = requiredTask(d.getTaskId());
        if ("COMPLETED".equals(task.getStatus())) {
            throw new ServiceException("所属任务已完成，不能再提交交付物；如需补交请先重新打开任务");
        }
        if (!"admin".equalsIgnoreCase(username)
                && (StringUtils.isBlank(task.getOwnerCode()) || !task.getOwnerCode().equalsIgnoreCase(username))) {
            throw new ServiceException("仅任务负责人或admin可以提交交付物");
        }
        if (!"PENDING".equals(d.getStatus()) && !"RETURNED".equals(d.getStatus())) {
            throw new ServiceException("当前交付物不允许提交");
        }
        if (submission.getFileUrl() == null && submission.getExternalUrl() == null) {
            throw new ServiceException("请上传文件或填写外部链接");
        }
        Integer next = mapper.selectNextVersion(id);
        submission.setDeliverableId(id);
        submission.setVersionNo(next == null ? 1 : next);
        submission.setSubmitBy(username);
        submission.setReviewResult("1".equals(d.getApprovalRequired()) ? "SUBMITTED" : "DELIVERED");
        mapper.insertSubmission(submission);
        d.setStatus("1".equals(d.getApprovalRequired()) ? "PENDING_APPROVAL" : "DELIVERED");
        d.setSubmitBy(username);
        d.setLatestFileUrl(submission.getFileUrl());
        d.setLatestExternalUrl(submission.getExternalUrl());
        mapper.updateStatus(d);
    }

    @Override
    public void review(Long id, boolean approved, String comment, String username) {
        if (!"admin".equals(username)) {
            throw new ServiceException("仅 admin 可以审核交付物");
        }
        ProjectDeliverable d = required(id);
        assertProjectAllowed(d.getProjectId());
        if (!"PENDING_APPROVAL".equals(d.getStatus())) {
            throw new ServiceException("当前交付物不在待审批状态");
        }
        if (!approved && (comment == null || comment.trim().isEmpty())) {
            throw new ServiceException("驳回时必须填写意见");
        }
        List<ProjectDeliverableSubmission> history = mapper.selectSubmissions(id);
        if (history.isEmpty()) {
            throw new ServiceException("未找到待审批提交记录");
        }
        ProjectDeliverableSubmission last = history.get(0);
        last.setReviewBy(username);
        last.setReviewComment(comment);
        last.setReviewResult(approved ? "APPROVED" : "RETURNED");
        mapper.updateSubmissionReview(last);
        d.setStatus(approved ? "APPROVED" : "RETURNED");
        mapper.updateStatus(d);
    }

    @Override
    public List<ProjectDeliverableSubmission> selectSubmissions(Long id) {
        return mapper.selectSubmissions(id);
    }

    private ProjectDeliverable required(Long id) {
        ProjectDeliverable d = mapper.selectById(id);
        if (d == null) {
            throw new ServiceException("交付物不存在");
        }
        return d;
    }

    private ProjectWorkItem requiredTask(Long taskId) {
        ProjectWorkItem task = taskMapper.selectById(taskId);
        if (task == null || !"TASK".equals(task.getItemType())) {
            throw new ServiceException("关联WBS任务不存在");
        }
        return task;
    }

    private void assertTaskEditable(Long taskId) {
        if ("COMPLETED".equals(requiredTask(taskId).getStatus())) {
            throw new ServiceException("所属任务已完成，不能调整交付物；如需调整请先重新打开任务");
        }
    }

    private void assertProjectAllowed(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if ("DRAFT".equals(project.getStatus())) {
            throw new ServiceException("项目处于申请草稿阶段，正式立项后才能维护交付物");
        }
    }
}
