package com.ruoyi.projectmanagement.deliverable.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.service.IBomMasterService;
import com.ruoyi.mes.base.service.IBomVersionService;
import com.ruoyi.mes.common.enums.BomApproveStatus;
import com.ruoyi.mes.common.enums.BomMasterStatus;
import com.ruoyi.mes.common.enums.BomVersionStatus;
import com.ruoyi.projectmanagement.common.enums.DeliverableStatus;
import com.ruoyi.projectmanagement.common.enums.DeliverableSubmissionStatus;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.deliverable.domain.BomDeliverableOption;
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
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import com.ruoyi.projectmanagement.workflow.service.WorkflowBusinessCallback;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** 项目交付物业务实现。 */
@Service
public class ProjectDeliverableServiceImpl
        implements IProjectDeliverableService, WorkflowBusinessCallback {

    private static final String FILE_MODE = "FILE";
    private static final String LINK_MODE = "LINK";
    private static final String BUSINESS_OBJECT_MODE = "BUSINESS_OBJECT";
    private static final String BOM_TYPE = "BOM";
    private static final String BOM_VERSION_BUSINESS_TYPE = "BOM_VERSION";

    private final ProjectDeliverableMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWbsMapper wbsMapper;
    private final IProjectTaskService taskService;
    private final ProjectDeliverableTypeMapper typeMapper;
    private final IBomMasterService bomMasterService;
    private final IBomVersionService bomVersionService;
    private final IWorkflowService workflowService;
    private final ObjectMapper objectMapper;

    public ProjectDeliverableServiceImpl(
            ProjectDeliverableMapper mapper,
            ProjectInfoMapper projectMapper,
            ProjectWbsMapper wbsMapper,
            IProjectTaskService taskService,
            ProjectDeliverableTypeMapper typeMapper,
            IBomMasterService bomMasterService,
            IBomVersionService bomVersionService,
            @Lazy IWorkflowService workflowService,
            ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.wbsMapper = wbsMapper;
        this.taskService = taskService;
        this.typeMapper = typeMapper;
        this.bomMasterService = bomMasterService;
        this.bomVersionService = bomVersionService;
        this.workflowService = workflowService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProjectDeliverable> selectList(ProjectDeliverable entity) {
        return mapper.selectList(entity);
    }

    @Override
    public List<ProjectDeliverable> selectMine(Long userId, ProjectDeliverable entity) {
        if (entity == null) {
            entity = new ProjectDeliverable();
        }
        // 我的待交付同时需要走项目可见范围校验；仅设置负责人筛选会命中
        // mapper 的无范围保护分支，导致负责人页面永远返回空列表。
        entity.setViewerUserId(userId);
        entity.setWorkPackageOwnerUserId(userId);
        return mapper.selectList(entity);
    }

    @Override
    public ProjectDeliverable selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BomDeliverableOption> selectBomOptions() {
        BomMaster masterFilter = new BomMaster();
        masterFilter.setStatus(BomMasterStatus.ENABLED.getCode());
        return bomMasterService.selectBomMasterList(masterFilter).stream()
                .flatMap(master -> selectableVersions(master).stream())
                .toList();
    }

    @Override
    public int insert(ProjectDeliverable entity) {
        assertRequirementMutable(entity.getProjectId());
        prepare(entity);
        assertPackageEditable(entity.getWorkPackageId());
        return mapper.insert(entity);
    }

    @Override
    public int update(ProjectDeliverable entity) {
        ProjectDeliverable old = required(entity.getDeliverableId());
        entity.setProjectId(old.getProjectId());
        entity.setWorkPackageId(old.getWorkPackageId());
        assertRequirementMutable(old.getProjectId());
        if (!DeliverableStatus.PENDING.matches(old.getStatus())
                && !DeliverableStatus.RETURNED.matches(old.getStatus())) {
            throw new ServiceException("已提交或已通过的交付要求不能直接修改");
        }
        prepare(entity);
        assertPackageEditable(entity.getWorkPackageId());
        return mapper.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            ProjectDeliverable d = required(id);
            assertRequirementMutable(d.getProjectId());
            assertPackageEditable(d.getWorkPackageId());
        }
        return mapper.deleteByIds(ids);
    }

    private void prepare(ProjectDeliverable entity) {
        if (projectMapper.selectProjectInfoById(entity.getProjectId()) == null) {
            throw new ServiceException("所属项目不存在");
        }
        ProjectWbsNode wp = wbsMapper.selectById(entity.getWorkPackageId());
        if (wp == null
                || !WbsNodeType.WORK_PACKAGE.matches(wp.getNodeType())
                || !wp.getProjectId().equals(entity.getProjectId())) {
            throw new ServiceException("所属工作包不存在或不属于当前项目");
        }
        if (entity.getPlannedDate() != null
                && ((wp.getPlanStartDate() != null
                                && entity.getPlannedDate().isBefore(wp.getPlanStartDate()))
                        || (wp.getPlanEndDate() != null
                                && entity.getPlannedDate().isAfter(wp.getPlanEndDate())))) {
            throw new ServiceException(
                    "计划交付日期必须在工作包周期内（"
                            + wp.getPlanStartDate()
                            + " ~ "
                            + wp.getPlanEndDate()
                            + "）");
        }
        ProjectDeliverableType type =
                entity.getDeliverableTypeId() == null
                        ? typeMapper.selectByCode(entity.getDeliverableType())
                        : typeMapper.selectById(entity.getDeliverableTypeId());
        if (type == null || !"0".equals(type.getStatus())) {
            throw new ServiceException("交付物类型不存在或已停用");
        }
        entity.setDeliverableTypeId(type.getTypeId());
        entity.setDeliverableType(type.getTypeCode());
        entity.setSubmissionMode(type.getSubmissionMode());
        if (FILE_MODE.equals(type.getSubmissionMode())) {
            List<String> configuredExtensions =
                    type.getAllowedExtensions() == null ? List.of() : type.getAllowedExtensions();
            Set<String> typeExtensions =
                    configuredExtensions.stream()
                            .map(x -> x.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toSet());
            Set<String> selectedExtensions = extensions(entity.getAllowedExtensions());
            if (!selectedExtensions.isEmpty() && !typeExtensions.containsAll(selectedExtensions)) {
                throw new ServiceException("允许格式必须属于交付物类型配置的格式范围");
            }
            entity.setAllowedExtensions(
                    String.join(
                            ",", selectedExtensions.isEmpty() ? typeExtensions : selectedExtensions));
        } else {
            entity.setAllowedExtensions(null);
        }
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
    @Transactional
    public void submit(
            Long id, ProjectDeliverableSubmission submission, String username, Long userId) {
        ProjectDeliverable d = required(id);
        assertProjectAllowed(d.getProjectId());
        ProjectWbsNode workPackage = requiredPackage(d.getWorkPackageId());
        assertSubmitterAllowed(workPackage, userId);
        if (!DeliverableStatus.PENDING.matches(d.getStatus())
                && !DeliverableStatus.RETURNED.matches(d.getStatus())) {
            throw new ServiceException("当前交付物不允许提交");
        }
        if (FILE_MODE.equals(d.getSubmissionMode())) {
            if (submission.getFileUrl() == null || submission.getFileUrl().isBlank())
                throw new ServiceException("请上传文件");
            String extension = extensionOf(submission.getFileUrl());
            Set<String> allowed = extensions(d.getAllowedExtensions());
            if (allowed.isEmpty()) {
                ProjectDeliverableType type =
                        d.getDeliverableTypeId() == null
                                ? typeMapper.selectByCode(d.getDeliverableType())
                                : typeMapper.selectById(d.getDeliverableTypeId());
                if (type != null && type.getAllowedExtensions() != null) {
                    allowed =
                            type.getAllowedExtensions().stream()
                                    .map(x -> x.toLowerCase(Locale.ROOT))
                                    .collect(Collectors.toSet());
                }
            }
            if (!allowed.isEmpty() && !allowed.contains(extension)) {
                String allowedText =
                        d.getAllowedExtensions() == null || d.getAllowedExtensions().isBlank()
                                ? allowed.stream().sorted().collect(Collectors.joining(","))
                                : d.getAllowedExtensions();
                throw new ServiceException("文件格式不符合交付要求，仅允许：" + allowedText);
            }
            submission.setExternalUrl(null);
            clearBusinessObject(submission);
        } else if (LINK_MODE.equals(d.getSubmissionMode())) {
            if (submission.getExternalUrl() == null || submission.getExternalUrl().isBlank())
                throw new ServiceException("请填写外部链接");
            String externalUrl = submission.getExternalUrl().trim();
            if (!isHttpUrl(externalUrl)) {
                throw new ServiceException("外部链接必须是有效的 http 或 https 地址");
            }
            submission.setExternalUrl(externalUrl);
            submission.setFileUrl(null);
            clearBusinessObject(submission);
        } else if (BUSINESS_OBJECT_MODE.equals(d.getSubmissionMode())) {
            prepareBomSubmission(d, submission);
        } else {
            throw new ServiceException("当前交付物提交方式暂不支持");
        }
        Integer next = mapper.selectNextVersion(id);
        submission.setDeliverableId(id);
        submission.setVersionNo(next == null ? 1 : next);
        submission.setSubmitBy(username);
        submission.setReviewResult(
                "1".equals(d.getApprovalRequired())
                        ? DeliverableSubmissionStatus.SUBMITTED.getCode()
                        : DeliverableSubmissionStatus.DELIVERED.getCode());
        mapper.insertSubmission(submission);
        if ("1".equals(d.getApprovalRequired())) {
            String snapshot;
            try {
                snapshot =
                        objectMapper.writeValueAsString(
                                java.util.Map.of("deliverable", d, "submission", submission));
            } catch (Exception exception) {
                throw new ServiceException("生成交付物审批快照失败");
            }
            Long instanceId =
                    workflowService.start(
                            "DELIVERABLE_APPROVAL",
                            submission.getSubmissionId(),
                            d.getProjectId(),
                            "交付物审批："
                                    + d.getDeliverableName()
                                    + "（V"
                                    + submission.getVersionNo()
                                    + "）",
                            snapshot,
                            username,
                            userId);
            submission.setWorkflowInstanceId(instanceId);
            mapper.bindSubmissionWorkflow(submission);
        }
        d.setStatus(
                "1".equals(d.getApprovalRequired())
                        ? DeliverableStatus.PENDING_APPROVAL.getCode()
                        : DeliverableStatus.DELIVERED.getCode());
        d.setSubmitBy(username);
        d.setLatestFileUrl(submission.getFileUrl());
        d.setLatestExternalUrl(submission.getExternalUrl());
        d.setBusinessType(submission.getBusinessType());
        d.setBusinessId(submission.getBusinessId());
        mapper.updateStatus(d);
        taskService.refreshPackage(d.getWorkPackageId());
    }

    /** 校验提交人必须是工作包负责人，防止绕过前端直接调用接口。 */
    private void assertSubmitterAllowed(ProjectWbsNode workPackage, Long userId) {
        if (!userId.equals(workPackage.getOwnerUserId())) {
            throw new ServiceException("只有工作包负责人可以提交交付物");
        }
    }

    /** 外链仅接受带主机名的 http/https 地址，避免保存无效或不安全的链接。 */
    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme())
                            || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** 查询某个 BOM 主数据下可交付的已审核版本。 */
    private List<BomDeliverableOption> selectableVersions(BomMaster master) {
        BomVersion versionFilter = new BomVersion();
        versionFilter.setBomMasterId(master.getId());
        return bomVersionService.selectBomVersionList(versionFilter).stream()
                .filter(this::isDeliverableBomVersion)
                .map(version -> toOption(master, version))
                .toList();
    }

    /** 业务对象交付当前仅支持关联一个已审核的 BOM 版本。 */
    private void prepareBomSubmission(
            ProjectDeliverable deliverable, ProjectDeliverableSubmission submission) {
        if (!BOM_TYPE.equals(deliverable.getDeliverableType())) {
            throw new ServiceException("当前仅BOM交付物支持业务对象提交");
        }
        Long versionId = parseBomVersionId(submission.getBusinessId());
        BomVersion version = bomVersionService.selectBomVersionById(versionId);
        if (version == null || !isDeliverableBomVersion(version)) {
            throw new ServiceException("请选择已审核且生效或冻结的BOM版本");
        }
        BomMaster master = bomMasterService.selectBomMasterById(version.getBomMasterId());
        if (master == null || !BomMasterStatus.ENABLED.getCode().equals(master.getStatus())) {
            throw new ServiceException("所选BOM主数据不存在或已停用");
        }
        submission.setBusinessType(BOM_VERSION_BUSINESS_TYPE);
        submission.setBusinessId(String.valueOf(version.getId()));
        submission.setBusinessCode(master.getBomCode());
        submission.setBusinessName(master.getParentItemName());
        submission.setBusinessVersion(version.getVersionCode());
        submission.setFileUrl(null);
        submission.setExternalUrl(null);
    }

    private Long parseBomVersionId(String businessId) {
        if (businessId == null || businessId.isBlank()) {
            throw new ServiceException("请选择要交付的BOM版本");
        }
        try {
            return Long.valueOf(businessId);
        } catch (NumberFormatException exception) {
            throw new ServiceException("BOM版本标识无效");
        }
    }

    private boolean isDeliverableBomVersion(BomVersion version) {
        return BomApproveStatus.APPROVED.getCode().equals(version.getApproveStatus())
                && (BomVersionStatus.EFFECTIVE.getCode().equals(version.getStatus())
                        || BomVersionStatus.FROZEN.getCode().equals(version.getStatus()));
    }

    private BomDeliverableOption toOption(BomMaster master, BomVersion version) {
        BomDeliverableOption option = new BomDeliverableOption();
        option.setBomMasterId(master.getId());
        option.setBomCode(master.getBomCode());
        option.setParentItemCode(master.getParentItemCode());
        option.setParentItemName(master.getParentItemName());
        option.setBomVersionId(version.getId());
        option.setVersionCode(version.getVersionCode());
        option.setVersionName(version.getVersionName());
        option.setStatus(version.getStatus());
        option.setApproveStatus(version.getApproveStatus());
        return option;
    }

    private void clearBusinessObject(ProjectDeliverableSubmission submission) {
        submission.setBusinessType(null);
        submission.setBusinessId(null);
        submission.setBusinessCode(null);
        submission.setBusinessName(null);
        submission.setBusinessVersion(null);
    }

    /** 审核交付物提交：按提交结果（APPROVED/RETURNED）更新交付物与最新提交记录。 */
    @Override
    public void review(Long id, ProjectDeliverableSubmission submission, String username) {
        throw new ServiceException("请在审批中心处理交付物审批");
    }

    @Override
    public String businessType() {
        return "DELIVERABLE_APPROVAL";
    }

    @Override
    @Transactional
    public void approved(Long submissionId, String operator, String opinion) {
        finishSubmission(submissionId, true, operator, opinion);
    }

    @Override
    @Transactional
    public void rejected(Long submissionId, String operator, String opinion) {
        finishSubmission(submissionId, false, operator, opinion);
    }

    private void finishSubmission(
            Long submissionId, boolean approved, String operator, String opinion) {
        ProjectDeliverableSubmission last = mapper.selectSubmissionById(submissionId);
        if (last == null
                || !DeliverableSubmissionStatus.SUBMITTED.matches(last.getReviewResult())) {
            throw new ServiceException("待审批交付物版本不存在");
        }
        ProjectDeliverable deliverable = required(last.getDeliverableId());
        last.setReviewBy(operator);
        last.setReviewComment(opinion);
        last.setReviewResult(
                approved
                        ? DeliverableSubmissionStatus.APPROVED.getCode()
                        : DeliverableSubmissionStatus.RETURNED.getCode());
        mapper.updateSubmissionReview(last);
        deliverable.setStatus(
                approved
                        ? DeliverableStatus.APPROVED.getCode()
                        : DeliverableStatus.RETURNED.getCode());
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

    /** 正式交付要求属于计划基线，只能在启动前维护。 */
    private void assertRequirementMutable(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (!ProjectStatus.APPROVED.matches(project.getStatus())) {
            throw new ServiceException("正式交付要求仅允许在项目已立项待启动阶段维护；执行中调整请走项目变更");
        }
    }

    /** 执行事实（提交、审批）不属于计划修改，保留原有可用状态范围。 */
    private void assertProjectAllowed(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("所属项目不存在");
        }
        if (ProjectStatus.DRAFT.matches(project.getStatus())
                || ProjectStatus.PENDING_APPROVAL.matches(project.getStatus())) {
            throw new ServiceException("项目处于申请草稿阶段，正式立项后才能维护交付物");
        }
        if (ProjectStatus.COMPLETED.matches(project.getStatus())) {
            throw new ServiceException("项目已完成，正式交付物只读");
        }
    }

    private Set<String> extensions(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(","))
                .map(x -> x.trim().replaceFirst("^\\.", "").toLowerCase(Locale.ROOT))
                .filter(x -> !x.isBlank())
                .collect(Collectors.toSet());
    }

    private String extensionOf(String fileUrl) {
        String path = fileUrl.split("[?#]", 2)[0];
        int index = path.lastIndexOf('.');
        if (index < 0 || index == path.length() - 1) throw new ServiceException("无法识别上传文件格式");
        return path.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
