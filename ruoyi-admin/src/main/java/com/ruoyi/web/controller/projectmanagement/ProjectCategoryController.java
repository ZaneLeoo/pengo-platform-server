package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import com.ruoyi.projectmanagement.category.service.IProjectCategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 项目分类控制器。 */
@RestController
@RequestMapping("/projectManagement/category")
public class ProjectCategoryController extends BaseController {
    private final IProjectCategoryService categoryService;
    public ProjectCategoryController(IProjectCategoryService categoryService) { this.categoryService = categoryService; }

    @PreAuthorize("@ss.hasPermi('projectManagement:category:list')")
    @GetMapping("/list") public AjaxResult list(ProjectCategory category) { return success(categoryService.selectProjectCategoryList(category)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:category:list')")
    @GetMapping("/tree") public AjaxResult tree() { return success(categoryService.selectProjectCategoryTree()); }
    @PreAuthorize("@ss.hasPermi('projectManagement:category:query')")
    @GetMapping("/{categoryId}") public AjaxResult getInfo(@PathVariable Long categoryId) { return success(categoryService.selectProjectCategoryById(categoryId)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:category:add')") @Log(title="项目分类",businessType=BusinessType.INSERT)
    @PostMapping public AjaxResult add(@Validated @RequestBody ProjectCategory category) { if(!categoryService.checkCategoryCodeUnique(category)) return error("分类编码已存在"); category.setCreateBy(getUsername()); return toAjax(categoryService.insertProjectCategory(category)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:category:edit')") @Log(title="项目分类",businessType=BusinessType.UPDATE)
    @PutMapping public AjaxResult edit(@Validated @RequestBody ProjectCategory category) { if(!categoryService.checkCategoryCodeUnique(category)) return error("分类编码已存在"); category.setUpdateBy(getUsername()); return toAjax(categoryService.updateProjectCategory(category)); }
    @PreAuthorize("@ss.hasPermi('projectManagement:category:remove')") @Log(title="项目分类",businessType=BusinessType.DELETE)
    @DeleteMapping("/{categoryId}") public AjaxResult remove(@PathVariable Long categoryId) { return toAjax(categoryService.deleteProjectCategoryById(categoryId)); }
}
