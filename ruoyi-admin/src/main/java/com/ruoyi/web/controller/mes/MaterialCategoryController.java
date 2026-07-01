package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.service.IMaterialCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

/**
 * 物料分类控制器。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mes/base/materialCategory")
public class MaterialCategoryController extends BaseController {

    @Autowired
    private IMaterialCategoryService categoryService;

    /**
     * 查询物料分类列表。
     */
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:list')")
    @GetMapping("/list")
    public AjaxResult list(MaterialCategory category) {
        List<MaterialCategory> list = categoryService.selectCategoryList(category);
        return success(list);
    }

    /**
     * 导出物料分类。
     */
    @Log(title = "物料分类", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MaterialCategory category) {
        List<MaterialCategory> list = categoryService.selectCategoryList(category);
        ExcelUtil<MaterialCategory> util = new ExcelUtil<>(MaterialCategory.class);
        util.exportExcel(response, list, "物料分类数据");
    }

    /**
     * 获取物料分类详情。
     */
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增物料分类。
     */
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:add')")
    @Log(title = "物料分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MaterialCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("新增物料分类'" + category.getCategoryName() + "'失败，分类编码已存在");
        }
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改物料分类。
     */
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:edit')")
    @Log(title = "物料分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MaterialCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("修改物料分类'" + category.getCategoryName() + "'失败，分类编码已存在");
        }
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除物料分类。
     */
    @PreAuthorize("@ss.hasPermi('mes:materialCategory:remove')")
    @Log(title = "物料分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId) {
        return toAjax(categoryService.deleteCategoryById(categoryId));
    }

    /**
     * 查询物料分类树选项。
     */
    @GetMapping("/treeSelect")
    public AjaxResult treeselect(MaterialCategory category) {
        return success(categoryService.selectCategoryList(category));
    }
}
