package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableTypeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 交付物类型与格式配置。 */
@RestController
@RequestMapping("/projectManagement/deliverable-type")
public class ProjectDeliverableTypeController extends BaseController {
    private final IProjectDeliverableTypeService service;

    public ProjectDeliverableTypeController(IProjectDeliverableTypeService service) {
        this.service = service;
    }

    @GetMapping("/list")
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:deliverableType:list')")
    public AjaxResult list(ProjectDeliverableType filter) {
        return success(service.list(filter));
    }

    @GetMapping("/{id}")
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:deliverableType:query')")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.get(id));
    }

    @PostMapping
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:deliverableType:add')")
    @Log(title = "交付物类型", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody ProjectDeliverableType entity) {
        entity.setCreateBy(getUsername());
        return toAjax(service.add(entity, getUsername()));
    }

    @PutMapping
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:deliverableType:edit')")
    @Log(title = "交付物类型", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@Validated @RequestBody ProjectDeliverableType entity) {
        entity.setUpdateBy(getUsername());
        return toAjax(service.edit(entity, getUsername()));
    }

    @DeleteMapping("/{id}")
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:deliverableType:remove')")
    @Log(title = "交付物类型", businessType = BusinessType.DELETE)
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.remove(id));
    }
}
