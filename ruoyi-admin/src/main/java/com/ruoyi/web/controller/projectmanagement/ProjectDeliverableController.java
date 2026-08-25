package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目交付物接口。
 */
@RestController
@RequestMapping("/projectManagement/deliverable")
public class ProjectDeliverableController extends BaseController {

    private final IProjectDeliverableService service;

    public ProjectDeliverableController(IProjectDeliverableService service) {
        this.service = service;
    }

    /** 查询交付物列表。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectDeliverable entity) {
        startPage();
        return getDataTable(service.selectList(entity));
    }

    /** 查询当前人员作为工作包负责人的正式交付物。 */
    @GetMapping("/mine")
    public TableDataInfo mine(ProjectDeliverable entity) {
        startPage();
        return getDataTable(service.selectMine(getUserId(), entity));
    }

    /** 查询交付物详细。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    /** 新增交付物。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:add')")
    @Log(title = "项目交付物", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectDeliverable entity) {
        entity.setCreateBy(getUsername());
        return toAjax(service.insert(entity));
    }

    /** 修改交付物。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')")
    @Log(title = "项目交付物", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProjectDeliverable entity) {
        entity.setUpdateBy(getUsername());
        return toAjax(service.update(entity));
    }

    /** 删除交付物。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }

    /** 提交交付物。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')")
    @Log(title = "提交交付物", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/submit")
    public AjaxResult submit(@PathVariable Long id, @RequestBody ProjectDeliverableSubmission entity) {
        service.submit(id, entity, getUsername(), getUserId());
        return success();
    }

    /** 审核交付物提交。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')")
    @Log(title = "审核交付物", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/review")
    public AjaxResult review(@PathVariable Long id, @RequestBody ProjectDeliverableSubmission entity) {
        service.review(id, entity, getUsername());
        return success();
    }

    /** 查询交付物提交与审核历史。 */
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:query')")
    @GetMapping("/{id}/submissions")
    public AjaxResult submissions(@PathVariable Long id) {
        return success(service.selectSubmissions(id));
    }
}
