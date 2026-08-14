package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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
    public ProjectDeliverableTypeController(IProjectDeliverableTypeService service) { this.service = service; }

    @GetMapping("/list")
    public AjaxResult list(ProjectDeliverableType filter) { return success(service.list(filter)); }
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) { return success(service.get(id)); }
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectDeliverableType entity) {
        entity.setCreateBy(getUsername());
        return toAjax(service.add(entity, getUsername()));
    }
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ProjectDeliverableType entity) {
        entity.setUpdateBy(getUsername());
        return toAjax(service.edit(entity, getUsername()));
    }
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) { return toAjax(service.remove(id)); }
}
