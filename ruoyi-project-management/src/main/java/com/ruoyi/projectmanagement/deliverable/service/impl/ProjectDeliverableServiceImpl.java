package com.ruoyi.projectmanagement.deliverable.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.common.enums.DeliverableStatus;
import com.ruoyi.projectmanagement.common.enums.DeliverableSubmissionStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableTypeMapper;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 项目交付物业务实现。
 */
@Service
public class ProjectDeliverableServiceImpl implements IProjectDeliverableService {

    private final ProjectDeliverableMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWbsMapper wbsMapper;
    private final IProjectTaskService taskService;
    private final ProjectDeliverableTypeMapper typeMapper;

    public ProjectDeliverableServiceImpl(ProjectDeliverableMapper mapper, ProjectInfoMapper projectMapper,
            ProjectWbsMapper wbsMapper, IProjectTaskService taskService, ProjectDeliverableTypeMapper typeMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.wbsMapper = wbsMapper;
        this.taskService = taskService;
        this.typeMapper = typeMapper;
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
        assertPackageEditable(entity.getWorkPackageId());
        return mapper.insert(entity);
    }

    @Override
    public int update(ProjectDeliverable entity) {
        assertProjectAllowed(entity.getProjectId());
        prepare(entity);
        assertPackageEditable(entity.getWorkPackageId());
        return mapper.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            ProjectDeliverable d = required(id);
            assertProjectAllowed(d.getProjectId());
            assertPackageEditable(d.getWorkPackageId());
        }
        return mapper.deleteByIds(ids);
    }

    private void prepare(ProjectDeliverable entity) {
        if (projectMapper.selectProjectInfoById(entity.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
        }
        ProjectWbsNode wp = wbsMapper.selectById(entity.getWorkPackageId());
        if (wp == null || !WbsNodeType.WORK_PACKAGE.matches(wp.getNodeType())
                || !wp.getProjectId().equals(entity.getProjectId())) {
            throw new ServiceException("所属工作包不存在或不属于当前项目");
        }
        ProjectDeliverableType type = entity.getDeliverableTypeId() == null
                ? typeMapper.selectByCode(entity.getDeliverableType()) : typeMapper.selectById(entity.getDeliverableTypeId());
        if (type == null || !"0".equals(type.getStatus())) {
            throw new ServiceException("交付物类型不存在或已停用");
        }
        entity.setDeliverableTypeId(type.getTypeId());
        entity.setDeliverableType(type.getTypeCode());
        entity.setSubmissionMode(type.getSubmissionMode());
        Set<String> typeExtensions = type.getAllowedExtensions().stream()
                .map(x -> x.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        Set<String> selectedExtensions = extensions(entity.getAllowedExtensions());
        if (!selectedExtensions.isEmpty() && !typeExtensions.containsAll(selectedExtensions)) {
            throw new ServiceException("允许格式必须属于交付物类型配置的格式范围");
        }
        entity.setAllowedExtensions(String.join(",", selectedExtensions.isEmpty() ? typeExtensions : selectedExtensions));
        if (entity.getRequiredFlag() == null) {
            entity.setRequiredFlag("1");
        }
        if (entity.getApprovalRequired() == null) {
            entity.setApprovalRequired(type.getDefaultApprovalRequired());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(DeliverableStatus.PENDING.getCode());
        }
    }

    @Override
    public void submit(Long id, ProjectDeliverableSubmission submission, String username) {
        ProjectDeliverable d = required(id);
        assertProjectAllowed(d.getProjectId());
        requiredPackage(d.getWorkPackageId());
        if (!DeliverableStatus.PENDING.matches(d.getStatus()) && !DeliverableStatus.RETURNED.matches(d.getStatus())) {
            throw new ServiceException("当前交付物不允许提交");
        }
        if ("FILE".equals(d.getSubmissionMode())) {
            if (submission.getFileUrl() == null || submission.getFileUrl().isBlank()) throw new ServiceException("请上传文件");
            String extension = extensionOf(submission.getFileUrl());
            if (!extensions(d.getAllowedExtensions()).isEmpty() && !extensions(d.getAllowedExtensions()).contains(extension)) {
                throw new ServiceException("文件格式不符合交付要求，仅允许：" + d.getAllowedExtensions());
            }
            submission.setExternalUrl(null);
        } else if ("LINK".equals(d.getSubmissionMode())) {
            if (submission.getExternalUrl() == null || submission.getExternalUrl().isBlank()) throw new ServiceException("请填写外部链接");
            submission.setFileUrl(null);
        } else {
            throw new ServiceException("当前交付物提交方式暂不支持");
        }
        Integer next = mapper.selectNextVersion(id);
        submission.setDeliverableId(id);
        submission.setVersionNo(next == null ? 1 : next);
        submission.setSubmitBy(username);
        submission.setReviewResult("1".equals(d.getApprovalRequired())
                ? DeliverableSubmissionStatus.SUBMITTED.getCode() : DeliverableSubmissionStatus.DELIVERED.getCode());
        mapper.insertSubmission(submission);
        d.setStatus("1".equals(d.getApprovalRequired()) ? DeliverableStatus.PENDING_APPROVAL.getCode()
                : DeliverableStatus.DELIVERED.getCode());
        d.setSubmitBy(username);
        d.setLatestFileUrl(submission.getFileUrl());
        d.setLatestExternalUrl(submission.getExternalUrl());
        mapper.updateStatus(d);
        taskService.refreshPackage(d.getWorkPackageId());
    }

    /**
     * 审核交付物提交：按提交结果（APPROVED/RETURNED）更新交付物与最新提交记录。
     */
    @Override
    public void review(Long id, ProjectDeliverableSubmission submission, String username) {
        if (!"admin".equals(username)) {
            throw new ServiceException("仅 admin 可以审核交付物");
        }
        ProjectDeliverable deliverable = required(id);
        assertProjectAllowed(deliverable.getProjectId());
        if (!DeliverableStatus.PENDING_APPROVAL.matches(deliverable.getStatus())) {
            throw new ServiceException("当前交付物不在待审批状态");
        }
        String result = submission.getReviewResult() == null ? null : submission.getReviewResult().trim();
        boolean approved = DeliverableSubmissionStatus.APPROVED.matches(result);
        if (!approved && !DeliverableSubmissionStatus.RETURNED.matches(result)) {
            throw new ServiceException("审核结果不正确");
        }
        if (!approved && (submission.getReviewComment() == null || submission.getReviewComment().trim().isEmpty())) {
            throw new ServiceException("驳回时必须填写意见");
        }
        List<ProjectDeliverableSubmission> history = mapper.selectSubmissions(id);
        if (history.isEmpty()) {
            throw new ServiceException("未找到待审批提交记录");
        }
        ProjectDeliverableSubmission last = history.get(0);
        last.setReviewBy(username);
        last.setReviewComment(submission.getReviewComment());
        last.setReviewResult(approved ? DeliverableSubmissionStatus.APPROVED.getCode()
                : DeliverableSubmissionStatus.RETURNED.getCode());
        mapper.updateSubmissionReview(last);
        deliverable.setStatus(approved ? DeliverableStatus.APPROVED.getCode() : DeliverableStatus.RETURNED.getCode());
        mapper.updateStatus(deliverable);
        taskService.refreshPackage(deliverable.getWorkPackageId());
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

    private ProjectWbsNode requiredPackage(Long id) {
        ProjectWbsNode wp = wbsMapper.selectById(id);
        if (wp == null || !WbsNodeType.WORK_PACKAGE.matches(wp.getNodeType())) {
            throw new ServiceException("所属工作包不存在");
        }
        return wp;
    }

    private void assertPackageEditable(Long id) {
        requiredPackage(id);
    }

    private void assertProjectAllowed(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (ProjectStatus.DRAFT.matches(project.getStatus()) || ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            throw new ServiceException("项目处于申请草稿阶段，正式立项后才能维护交付物");
        }
    }

    private Set<String> extensions(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(",")).map(x -> x.trim().replaceFirst("^\\.", "").toLowerCase(Locale.ROOT))
                .filter(x -> !x.isBlank()).collect(Collectors.toSet());
    }

    private String extensionOf(String fileUrl) {
        String path = fileUrl.split("[?#]", 2)[0];
        int index = path.lastIndexOf('.');
        if (index < 0 || index == path.length() - 1) throw new ServiceException("无法识别上传文件格式");
        return path.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
