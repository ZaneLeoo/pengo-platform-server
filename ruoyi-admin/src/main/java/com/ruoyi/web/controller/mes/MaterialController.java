package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.service.IMaterialService;
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
 * 物料主数据控制器。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mes/material")
public class MaterialController extends BaseController {

    @Autowired
    private IMaterialService materialService;

    /**
     * 查询物料列表。
     */
    @PreAuthorize("@ss.hasPermi('mes:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(Material material) {
        startPage();
        List<Material> list = materialService.selectMaterialList(material);
        return getDataTable(list);
    }

    /**
     * 导出物料列表。
     */
    @Log(title = "物料主数据", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mes:material:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Material material) {
        List<Material> list = materialService.selectMaterialList(material);
        ExcelUtil<Material> util = new ExcelUtil<>(Material.class);
        util.exportExcel(response, list, "物料主数据");
    }

    /**
     * 获取物料详情。
     */
    @PreAuthorize("@ss.hasPermi('mes:material:query')")
    @GetMapping("/{materialId}")
    public AjaxResult getInfo(@PathVariable Long materialId) {
        return success(materialService.selectMaterialById(materialId));
    }

    /**
     * 新增物料。
     */
    @PreAuthorize("@ss.hasPermi('mes:material:add')")
    @Log(title = "物料主数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Material material) {
        if (!materialService.checkMaterialCodeUnique(material)) {
            return error("新增物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setCreateBy(getUsername());
        return toAjax(materialService.insertMaterial(material));
    }

    /**
     * 修改物料。
     */
    @PreAuthorize("@ss.hasPermi('mes:material:edit')")
    @Log(title = "物料主数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Material material) {
        if (!materialService.checkMaterialCodeUnique(material)) {
            return error("修改物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setUpdateBy(getUsername());
        return toAjax(materialService.updateMaterial(material));
    }

    /**
     * 删除物料。
     */
    @PreAuthorize("@ss.hasPermi('mes:material:remove')")
    @Log(title = "物料主数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds) {
        return toAjax(materialService.deleteMaterialByIds(materialIds));
    }
}
