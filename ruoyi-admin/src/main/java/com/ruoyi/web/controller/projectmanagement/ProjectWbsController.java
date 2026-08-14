package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWorkPackageCreateRequest;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目WBS接口。
 */
@RestController
@RequestMapping("/projectManagement/wbs")
public class ProjectWbsController extends BaseController {

    private final IProjectWbsService service;

    public ProjectWbsController(IProjectWbsService service) {
        this.service = service;
    }

    /** 查询WBS树。 */
    @GetMapping("/tree")
    public AjaxResult tree(ProjectWbsNode filter) {
        return success(service.list(filter));
    }

    /** 查询WBS节点详细。 */
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.get(id));
    }

    /** 新增WBS节点，返回新节点ID。 */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectWbsNode node) {
        node.setCreateBy(getUsername());
        return AjaxResult.success("新增成功", service.add(node, getUsername()));
    }

    /** 创建工作包并一并定义初始交付要求。 */
    @PostMapping("/work-package")
    public AjaxResult addWorkPackage(@Validated @RequestBody ProjectWorkPackageCreateRequest request) {
        request.getWorkPackage().setCreateBy(getUsername());
        return AjaxResult.success("新增成功", service.addWorkPackage(request, getUsername()));
    }

    /** 修改WBS节点。 */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ProjectWbsNode node) {
        node.setUpdateBy(getUsername());
        return toAjax(service.edit(node, getUsername()));
    }

    /** 删除WBS节点。 */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.remove(id, getUsername()));
    }
}
