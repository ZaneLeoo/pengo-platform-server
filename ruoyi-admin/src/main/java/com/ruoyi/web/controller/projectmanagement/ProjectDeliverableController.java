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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projectManagement/deliverable")
public class ProjectDeliverableController extends BaseController {
    private final IProjectDeliverableService service;
    public ProjectDeliverableController(IProjectDeliverableService service) { this.service = service; }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:list')") @GetMapping("/list") public TableDataInfo list(ProjectDeliverable entity) { startPage(); return getDataTable(service.selectList(entity)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable Long id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:add')") @Log(title="项目交付物",businessType=BusinessType.INSERT) @PostMapping public AjaxResult add(@RequestBody ProjectDeliverable entity) { entity.setCreateBy(getUsername()); return toAjax(service.insert(entity)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')") @Log(title="项目交付物",businessType=BusinessType.UPDATE) @PutMapping public AjaxResult edit(@RequestBody ProjectDeliverable entity) { entity.setUpdateBy(getUsername()); return toAjax(service.update(entity)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:remove')") @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable Long[] ids) { return toAjax(service.deleteByIds(ids)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')") @Log(title="提交交付物",businessType=BusinessType.UPDATE) @PostMapping("/{id}/submit") public AjaxResult submit(@PathVariable Long id,@RequestBody ProjectDeliverableSubmission entity) { service.submit(id,entity,getUsername()); return success(); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')") @Log(title="审核交付物",businessType=BusinessType.UPDATE) @PostMapping("/{id}/review") public AjaxResult review(@PathVariable Long id,@RequestBody ProjectDeliverableSubmission entity) { service.review(id,"APPROVED".equals(entity.getReviewResult()),entity.getReviewComment(),getUsername()); return success(); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:query')") @GetMapping("/{id}/submissions") public AjaxResult submissions(@PathVariable Long id) { return success(service.selectSubmissions(id)); }
}
