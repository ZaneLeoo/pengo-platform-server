package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import com.ruoyi.projectmanagement.execution.service.IProjectWorkItemService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** WBS任务、交付物和问题的统一控制器。 */
@RestController
@RequestMapping("/projectManagement/workItem")
public class ProjectWorkItemController extends BaseController {
    private final IProjectWorkItemService service;
    public ProjectWorkItemController(IProjectWorkItemService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:list')")
    @GetMapping("/list") public TableDataInfo list(ProjectWorkItem item) { startPage(); return getDataTable(service.selectList(item)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:query')")
    @GetMapping("/{id}") public AjaxResult get(@PathVariable Long id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:list')")
    @GetMapping("/overview") public AjaxResult overview() { return success(service.overview()); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:add')")
    @Log(title = "项目执行项", businessType = BusinessType.INSERT)
    @PostMapping public AjaxResult add(@Validated @RequestBody ProjectWorkItem item) {
        item.setCreateBy(getUsername());
        int rows = service.insert(item);
        AjaxResult result = toAjax(rows);
        if (rows > 0) {
            result.put("data", item.getItemId());
        }
        return result;
    }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:edit')")
    @Log(title = "项目执行项", businessType = BusinessType.UPDATE)
    @PutMapping public AjaxResult edit(@Validated @RequestBody ProjectWorkItem item) { item.setUpdateBy(getUsername()); return toAjax(service.update(item)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:workItem:remove')")
    @Log(title = "项目执行项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable Long[] ids) { return toAjax(service.deleteByIds(ids)); }
}
