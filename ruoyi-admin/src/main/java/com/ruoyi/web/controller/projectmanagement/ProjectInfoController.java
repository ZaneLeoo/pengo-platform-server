package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.project.domain.InitiationReviewRequest;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.domain.ProjectInitiationAttachment;
import com.ruoyi.projectmanagement.project.domain.ProjectPreliminaryPlan;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目台账控制器。 */
@RestController
@RequestMapping("/projectManagement/project")
public class ProjectInfoController extends BaseController {

    private final IProjectInfoService service;

    public ProjectInfoController(IProjectInfoService service) {
        this.service = service;
    }

    /** 查询项目列表。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectInfo project) {
        startPage();
        List<ProjectInfo> list = service.selectProjectInfoList(project);
        return getDataTable(list);
    }

    /** 查询项目详细。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.selectProjectInfoById(id));
    }

    /** 新增项目申请。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:add')")
    @Log(title = "项目申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectInfo project) {
        if (!service.checkProjectCodeUnique(project)) {
            return error("项目编码已存在");
        }
        project.setCreateBy(getUsername());
        project.setApplicantDeptId(getDeptId());
        int rows = service.insertProjectInfo(project);
        return rows > 0 ? success(project) : error();
    }

    /** 修改项目申请。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @Log(title = "项目申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ProjectInfo project) {
        if (!service.checkProjectCodeUnique(project)) {
            return error("项目编码已存在");
        }
        project.setUpdateBy(getUsername());
        return toAjax(service.updateProjectInfo(project));
    }

    /** 执行项目生命周期动作（启动/暂停/恢复/完成）。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @Log(title = "项目生命周期", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/lifecycle")
    public AjaxResult lifecycle(
            @PathVariable Long id, @Validated @RequestBody LifecycleActionRequest request) {
        return toAjax(service.applyLifecycleAction(id, request, getUsername()));
    }

    /** 查询项目初步计划列表。 */
    @GetMapping("/{id}/preliminary-plans")
    public AjaxResult plans(@PathVariable Long id) {
        return success(service.preliminaryPlans(id));
    }

    /** 新增初步计划。 */
    @PostMapping("/{id}/preliminary-plans")
    public AjaxResult addPlan(
            @PathVariable Long id, @Validated @RequestBody ProjectPreliminaryPlan plan) {
        plan.setProjectId(id);
        return toAjax(service.addPreliminaryPlan(plan, getUsername()));
    }

    /** 修改初步计划。 */
    @PutMapping("/preliminary-plan")
    public AjaxResult editPlan(@Validated @RequestBody ProjectPreliminaryPlan plan) {
        return toAjax(service.updatePreliminaryPlan(plan, getUsername()));
    }

    /** 删除初步计划。 */
    @DeleteMapping("/preliminary-plan/{id}")
    public AjaxResult deletePlan(@PathVariable Long id) {
        return toAjax(service.deletePreliminaryPlan(id, getUsername()));
    }

    /** 提交立项审批。 */
    @PostMapping("/{id}/initiation/submit")
    public AjaxResult submit(@PathVariable Long id) {
        return toAjax(service.submitInitiation(id, getUsername(), getUserId()));
    }

    /** 立项审批。 */
    @PostMapping("/{id}/initiation/review")
    public AjaxResult review(
            @PathVariable Long id, @Validated @RequestBody InitiationReviewRequest request) {
        return toAjax(service.reviewInitiation(id, request, getUsername()));
    }

    /** 查询立项审批历史。 */
    @GetMapping("/{id}/initiation/approvals")
    public AjaxResult approvals(@PathVariable Long id) {
        return success(service.approvalHistory(id));
    }

    /** 查询立项审批快照。 */
    @GetMapping("/{id}/initiation/approvals/{approvalId}")
    public AjaxResult snapshot(@PathVariable Long id, @PathVariable Long approvalId) {
        return success(service.approvalSnapshot(id, approvalId));
    }

    /** 查询当前立项申请支撑材料。 */
    @GetMapping("/{id}/initiation/attachments")
    public AjaxResult initiationAttachments(@PathVariable Long id, String sectionCode) {
        return success(service.initiationAttachments(id, sectionCode));
    }

    /** 查询指定立项审批版本的支撑材料。 */
    @GetMapping("/{id}/initiation/approvals/{approvalId}/attachments")
    public AjaxResult initiationApprovalAttachments(
            @PathVariable Long id, @PathVariable Long approvalId, String sectionCode) {
        return success(service.initiationApprovalAttachments(id, approvalId, sectionCode));
    }

    /** 新增立项申请支撑材料。 */
    @PostMapping("/{id}/initiation/attachments")
    public AjaxResult addInitiationAttachment(
            @PathVariable Long id, @Validated @RequestBody ProjectInitiationAttachment attachment) {
        attachment.setProjectId(id);
        return toAjax(service.addInitiationAttachment(attachment, getUsername()));
    }

    /** 删除当前草稿支撑材料。 */
    @DeleteMapping("/{id}/initiation/attachments/{attachmentId}")
    public AjaxResult deleteInitiationAttachment(
            @PathVariable Long id, @PathVariable Long attachmentId) {
        return toAjax(service.deleteInitiationAttachment(id, attachmentId, getUsername()));
    }

    /** 批量删除项目。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:remove')")
    @Log(title = "项目台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteProjectInfoByIds(ids));
    }
}
