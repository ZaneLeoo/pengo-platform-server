package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目成本类别配置接口。 */
@RestController
@RequestMapping("/projectManagement/cost-category")
public class CostCategoryController extends BaseController {

    private final ICostCategoryService service;

    public CostCategoryController(ICostCategoryService service) {
        this.service = service;
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:list')")
    @GetMapping("/tree")
    public AjaxResult tree(CostCategory filter) {
        return success(service.tree(filter));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:query')")
    @GetMapping("/options")
    public AjaxResult options() {
        return success(service.options());
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.get(id));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:query')")
    @GetMapping("/{id}/usage")
    public AjaxResult usage(@PathVariable Long id) {
        return success(service.usage(id));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:add')")
    @Log(title = "项目成本类别", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody CostCategory category) {
        return toAjax(service.add(category, getUsername()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:edit')")
    @Log(title = "项目成本类别", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody CostCategory category) {
        return toAjax(service.edit(category, getUsername()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:changeStatus')")
    @Log(title = "项目成本类别状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return toAjax(service.changeStatus(id, body.get("status"), getUsername()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:costCategory:remove')")
    @Log(title = "项目成本类别", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.remove(id));
    }
}
