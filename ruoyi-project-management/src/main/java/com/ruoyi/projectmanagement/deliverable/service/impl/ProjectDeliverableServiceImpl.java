package com.ruoyi.projectmanagement.deliverable.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableService;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.mapper.ProjectWorkItemMapper;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectDeliverableServiceImpl implements IProjectDeliverableService {
    private final ProjectDeliverableMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWorkItemMapper taskMapper;
    public ProjectDeliverableServiceImpl(ProjectDeliverableMapper mapper, ProjectInfoMapper projectMapper, ProjectWorkItemMapper taskMapper) { this.mapper = mapper; this.projectMapper = projectMapper; this.taskMapper = taskMapper; }
    public List<ProjectDeliverable> selectList(ProjectDeliverable entity) { return mapper.selectList(entity); }
    public ProjectDeliverable selectById(Long id) { return mapper.selectById(id); }
    public int insert(ProjectDeliverable entity) { prepare(entity); return mapper.insert(entity); }
    public int update(ProjectDeliverable entity) { prepare(entity); return mapper.update(entity); }
    public int deleteByIds(Long[] ids) { return mapper.deleteByIds(ids); }
    private void prepare(ProjectDeliverable entity) {
        if (projectMapper.selectProjectInfoById(entity.getProjectId()) == null) throw new ServiceException("所属项目不存在");
        ProjectWorkItem task = taskMapper.selectById(entity.getTaskId());
        if (task == null || !"TASK".equals(task.getItemType()) || !task.getProjectId().equals(entity.getProjectId())) throw new ServiceException("关联WBS任务不存在或不属于当前项目");
        if (entity.getRequiredFlag() == null) entity.setRequiredFlag("1");
        if (entity.getApprovalRequired() == null) entity.setApprovalRequired("0");
        entity.setReviewer("admin");
        if (entity.getStatus() == null) entity.setStatus("PENDING");
    }
    public void submit(Long id, ProjectDeliverableSubmission submission, String username) {
        ProjectDeliverable d = required(id);
        if (submission.getFileUrl() == null && submission.getExternalUrl() == null) throw new ServiceException("请上传文件或填写外部链接");
        Integer next = mapper.selectNextVersion(id); submission.setDeliverableId(id); submission.setVersionNo(next == null ? 1 : next); submission.setSubmitBy(username);
        submission.setReviewResult("1".equals(d.getApprovalRequired()) ? "SUBMITTED" : "DELIVERED"); mapper.insertSubmission(submission);
        d.setStatus("1".equals(d.getApprovalRequired()) ? "PENDING_APPROVAL" : "DELIVERED"); d.setSubmitBy(username); d.setLatestFileUrl(submission.getFileUrl()); d.setLatestExternalUrl(submission.getExternalUrl()); mapper.updateStatus(d);
    }
    public void review(Long id, boolean approved, String comment, String username) {
        if (!"admin".equals(username)) throw new ServiceException("仅 admin 可以审核交付物");
        ProjectDeliverable d = required(id); if (!"PENDING_APPROVAL".equals(d.getStatus())) throw new ServiceException("当前交付物不在待审批状态");
        if (!approved && (comment == null || comment.trim().isEmpty())) throw new ServiceException("驳回时必须填写意见");
        List<ProjectDeliverableSubmission> history = mapper.selectSubmissions(id); if (history.isEmpty()) throw new ServiceException("未找到待审批提交记录");
        ProjectDeliverableSubmission last = history.get(0); last.setReviewBy(username); last.setReviewComment(comment); last.setReviewResult(approved ? "APPROVED" : "RETURNED"); mapper.updateSubmissionReview(last);
        d.setStatus(approved ? "APPROVED" : "RETURNED"); mapper.updateStatus(d);
    }
    public List<ProjectDeliverableSubmission> selectSubmissions(Long id) { return mapper.selectSubmissions(id); }
    private ProjectDeliverable required(Long id) { ProjectDeliverable d = mapper.selectById(id); if (d == null) throw new ServiceException("交付物不存在"); return d; }
}
