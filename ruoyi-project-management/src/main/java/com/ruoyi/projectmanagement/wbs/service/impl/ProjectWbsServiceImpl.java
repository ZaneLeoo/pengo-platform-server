package com.ruoyi.projectmanagement.wbs.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.common.enums.DeliverableStatus;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWorkPackageCreateRequest;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WBS范围树与工作包业务实现。
 */
@Service
public class ProjectWbsServiceImpl implements IProjectWbsService {

    private final ProjectWbsMapper mapper;
    private final ProjectInfoMapper projectMapper;
    private final IProjectTeamService teamService;
    private final ProjectDeliverableMapper deliverableMapper;

    public ProjectWbsServiceImpl(ProjectWbsMapper mapper, ProjectInfoMapper projectMapper,
            IProjectTeamService teamService, ProjectDeliverableMapper deliverableMapper) {
        this.mapper = mapper;
        this.projectMapper = projectMapper;
        this.teamService = teamService;
        this.deliverableMapper = deliverableMapper;
    }

    /** 查询WBS节点列表。 */
    @Override
    public List<ProjectWbsNode> list(ProjectWbsNode filter) {
        return mapper.selectList(filter);
    }

    /** 查询WBS节点详细，不存在时抛异常。 */
    @Override
    public ProjectWbsNode get(Long id) {
        return required(id);
    }

    /** 新增WBS节点，生成层级编码。 */
    @Override
    @Transactional
    public Long add(ProjectWbsNode node, String operator) {
        assertStructureMutable(node.getProjectId());
        normalize(node);
        validate(node, null);
        node.setWbsCode(nextCode(node.getProjectId(), node.getParentId()));
        node.setStatus(WbsStatus.NOT_STARTED.getCode());
        node.setProgress(0);
        node.setCreateBy(operator);
        if (mapper.insert(node) == 0) {
            throw new ServiceException("新增WBS节点失败");
        }
        return node.getWbsId();
    }

    /** 创建工作包和初始交付要求，任一交付要求校验失败时整体回滚。 */
    @Override
    @Transactional
    public Long addWorkPackage(ProjectWorkPackageCreateRequest request, String operator) {
        ProjectWbsNode workPackage = request.getWorkPackage();
        if (!WbsNodeType.WORK_PACKAGE.matches(workPackage.getNodeType())) {
            throw new ServiceException("该接口仅用于创建工作包");
        }
        Long workPackageId = add(workPackage, operator);
        for (ProjectDeliverable deliverable : request.getDeliverables() == null
                ? Collections.<ProjectDeliverable>emptyList() : request.getDeliverables()) {
            deliverable.setProjectId(workPackage.getProjectId());
            deliverable.setWorkPackageId(workPackageId);
            deliverable.setCreateBy(operator);
            deliverable.setRequiredFlag(deliverable.getRequiredFlag() == null ? "1" : deliverable.getRequiredFlag());
            deliverable.setApprovalRequired(deliverable.getApprovalRequired() == null ? "0" : deliverable.getApprovalRequired());
            deliverable.setStatus(DeliverableStatus.PENDING.getCode());
            if (deliverableMapper.insert(deliverable) == 0) {
                throw new ServiceException("新增工作包交付要求失败");
            }
        }
        return workPackageId;
    }

    /** 修改WBS节点，移动节点时重排受影响分支编码。 */
    @Override
    @Transactional
    public int edit(ProjectWbsNode node, String operator) {
        ProjectWbsNode old = required(node.getWbsId());
        assertStructureMutable(old.getProjectId());
        node.setProjectId(old.getProjectId());
        normalize(node);
        validate(node, old);
        boolean moved = !node.getParentId().equals(old.getParentId());
        node.setUpdateBy(operator);
        int rows = mapper.update(node);
        if (moved) {
            recodeChildren(old.getProjectId(), old.getParentId());
            recodeChildren(old.getProjectId(), node.getParentId());
        }
        refreshProject(old.getProjectId());
        return rows;
    }

    /** 删除WBS节点，非空节点不允许删除。 */
    @Override
    @Transactional
    public int remove(Long id, String operator) {
        ProjectWbsNode node = required(id);
        assertStructureMutable(node.getProjectId());
        if (mapper.countChildren(id) > 0 || mapper.countTasks(id) > 0 || mapper.countDeliverables(id) > 0) {
            throw new ServiceException("WBS节点非空，不能删除");
        }
        int rows = mapper.deleteById(id);
        recodeChildren(node.getProjectId(), node.getParentId());
        refreshProject(node.getProjectId());
        return rows;
    }

    /** 判断项目是否所有工作包均已完成。 */
    @Override
    public boolean allWorkPackagesCompleted(Long projectId) {
        ProjectWbsNode filter = new ProjectWbsNode();
        filter.setProjectId(projectId);
        filter.setNodeType(WbsNodeType.WORK_PACKAGE.getCode());
        List<ProjectWbsNode> packages = mapper.selectList(filter);
        return !packages.isEmpty() && packages.stream().allMatch(x -> WbsStatus.COMPLETED.matches(x.getStatus()));
    }

    /** 自底向上刷新项目内全部WBS节点的汇总状态与进度。 */
    @Override
    @Transactional
    public void refreshProject(Long projectId) {
        List<ProjectWbsNode> all = listForProject(projectId);
        all.stream()
                .sorted(Comparator.comparingInt(x -> -depth(x, all)))
                .forEach(x -> refreshNode(x, all));
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project != null) {
            List<ProjectWbsNode> roots = all.stream().filter(x -> x.getParentId() == 0).toList();
            project.setProgress(roots.isEmpty()
                    ? 0
                    : (int) Math.round(roots.stream()
                            .mapToInt(x -> x.getProgress() == null ? 0 : x.getProgress())
                            .average()
                            .orElse(0)));
            project.setUpdateBy("system");
            projectMapper.updateLifecycle(project);
        }
    }

    /** 汇总WBS节点按下级均值刷新日期、状态与进度。 */
    private void refreshNode(ProjectWbsNode node, List<ProjectWbsNode> all) {
        if (WbsNodeType.WORK_PACKAGE.matches(node.getNodeType())) {
            return;
        }
        List<ProjectWbsNode> children = all.stream()
                .filter(x -> node.getWbsId().equals(x.getParentId()))
                .toList();
        if (children.isEmpty()) {
            mapper.updateAggregate(node.getWbsId(), null, null, WbsStatus.NOT_STARTED.getCode(), 0);
            return;
        }
        LocalDate start = children.stream()
                .map(ProjectWbsNode::getPlanStartDate)
                .filter(x -> x != null)
                .min(LocalDate::compareTo)
                .orElse(null);
        LocalDate end = children.stream()
                .map(ProjectWbsNode::getPlanEndDate)
                .filter(x -> x != null)
                .max(LocalDate::compareTo)
                .orElse(null);
        int progress = (int) Math.round(children.stream()
                .mapToInt(x -> x.getProgress() == null ? 0 : x.getProgress())
                .average()
                .orElse(0));
        String status = progress == 100
                ? WbsStatus.COMPLETED.getCode()
                : progress > 0 ? WbsStatus.ACTIVE.getCode() : WbsStatus.NOT_STARTED.getCode();
        mapper.updateAggregate(node.getWbsId(), start, end, status, progress);
        node.setPlanStartDate(start);
        node.setPlanEndDate(end);
        node.setStatus(status);
        node.setProgress(progress);
    }

    /**
     * 校验WBS节点：层级关系、工作包必填项与项目周期约束。
     */
    private void validate(ProjectWbsNode node, ProjectWbsNode old) {
        ProjectInfo project = projectMapper.selectProjectInfoById(node.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (node.getParentId() != 0) {
            ProjectWbsNode parent = required(node.getParentId());
            if (!node.getProjectId().equals(parent.getProjectId())) {
                throw new ServiceException("上级WBS不属于当前项目");
            }
            if (WbsNodeType.WORK_PACKAGE.matches(parent.getNodeType())) {
                throw new ServiceException("工作包不能包含下级WBS");
            }
            if (old != null && isDescendant(node.getWbsId(), parent)) {
                throw new ServiceException("不能移动到自己的下级节点");
            }
        }
        if (WbsNodeType.WORK_PACKAGE.matches(node.getNodeType())) {
            if (node.getOwnerId() == null || !teamService.isActiveMember(node.getProjectId(), node.getOwnerId())) {
                throw new ServiceException("工作包负责人必须是当前项目在组成员");
            }
            if (node.getPlanStartDate() == null || node.getPlanEndDate() == null) {
                throw new ServiceException("请填写工作包计划日期");
            }
            if (node.getPlanEndDate().isBefore(node.getPlanStartDate())) {
                throw new ServiceException("工作包结束日期不能早于开始日期");
            }
            if (node.getPlanStartDate().isBefore(project.getStartDate())
                    || node.getPlanEndDate().isAfter(project.getEndDate())) {
                throw new ServiceException("工作包计划日期必须在项目周期内（" + project.getStartDate() + " ~ "
                        + project.getEndDate() + "）");
            }
            if (node.getAcceptanceCriteria() == null || node.getAcceptanceCriteria().isBlank()
                    || node.getDefinitionOfDone() == null || node.getDefinitionOfDone().isBlank()) {
                throw new ServiceException("工作包必须填写验收标准和完成定义");
            }
            if (old != null && WbsNodeType.SUMMARY.matches(old.getNodeType()) && mapper.countChildren(old.getWbsId()) > 0) {
                throw new ServiceException("汇总WBS仍有下级，不能转为工作包");
            }
        } else if (old != null && WbsNodeType.WORK_PACKAGE.matches(old.getNodeType())
                && (mapper.countTasks(old.getWbsId()) > 0 || mapper.countDeliverables(old.getWbsId()) > 0)) {
            throw new ServiceException("工作包已有任务或交付物，不能转为汇总WBS");
        }
    }

    /** 判断候选节点是否位于指定节点的下级分支。 */
    private boolean isDescendant(Long id, ProjectWbsNode candidate) {
        Long parentId = candidate.getParentId();
        while (parentId != null && parentId != 0) {
            if (id.equals(parentId)) {
                return true;
            }
            parentId = required(parentId).getParentId();
        }
        return false;
    }

    /** 补齐默认值。 */
    private void normalize(ProjectWbsNode node) {
        if (node.getParentId() == null) {
            node.setParentId(0L);
        }
        if (node.getSortOrder() == null) {
            node.setSortOrder(0);
        }
    }

    /** 按同级序号生成WBS编码，如 1、1.1、1.1.2。 */
    private String nextCode(Long projectId, Long parentId) {
        List<ProjectWbsNode> siblings = mapper.selectChildren(projectId, parentId);
        String prefix = parentId == 0 ? "" : required(parentId).getWbsCode() + ".";
        return prefix + (siblings.size() + 1);
    }

    /** 递归重排某分支下全部节点的编码。 */
    private void recodeChildren(Long projectId, Long parentId) {
        List<ProjectWbsNode> children = mapper.selectChildren(projectId, parentId);
        String prefix = parentId == 0 ? "" : required(parentId).getWbsCode() + ".";
        for (int i = 0; i < children.size(); i++) {
            ProjectWbsNode child = children.get(i);
            String code = prefix + (i + 1);
            mapper.updateCode(child.getWbsId(), code);
            child.setWbsCode(code);
            recodeChildren(projectId, child.getWbsId());
        }
    }

    /** 计算节点在树中的深度。 */
    private int depth(ProjectWbsNode node, List<ProjectWbsNode> all) {
        int depth = 0;
        Long parentId = node.getParentId();
        while (parentId != null && parentId != 0) {
            depth++;
            Long id = parentId;
            parentId = all.stream()
                    .filter(x -> x.getWbsId().equals(id))
                    .map(ProjectWbsNode::getParentId)
                    .findFirst()
                    .orElse(0L);
        }
        return depth;
    }

    /** 查询项目全部WBS节点。 */
    private List<ProjectWbsNode> listForProject(Long projectId) {
        ProjectWbsNode filter = new ProjectWbsNode();
        filter.setProjectId(projectId);
        return mapper.selectList(filter);
    }

    /** 查询WBS节点，不存在时抛异常。 */
    private ProjectWbsNode required(Long id) {
        ProjectWbsNode node = mapper.selectById(id);
        if (node == null) {
            throw new ServiceException("WBS节点不存在");
        }
        return node;
    }

    /** 校验项目处于已立项待启动状态，允许调整计划结构。 */
    private void assertStructureMutable(Long projectId) {
        ProjectInfo project = projectMapper.selectProjectInfoById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (!ProjectStatus.APPROVED.matches(project.getStatus())) {
            throw new ServiceException("只有已立项待启动项目可以调整计划结构");
        }
    }
}
