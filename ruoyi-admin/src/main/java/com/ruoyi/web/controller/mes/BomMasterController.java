package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.service.IBomMasterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * BOM主表控制器。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mes/base/bomMaster")
public class BomMasterController extends BaseController {
    @Autowired
    private IBomMasterService bomMasterService;

    /**
     * 查询BOM主表列表。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:list')")
    @GetMapping("/list")
    public TableDataInfo list(BomMaster bomMaster) {
        startPage();
        return getDataTable(bomMasterService.selectBomMasterList(bomMaster));
    }

    /**
     * 导出BOM主表。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:export')")
    @Log(title = "BOM主表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BomMaster bomMaster) {
        List<BomMaster> list = bomMasterService.selectBomMasterList(bomMaster);
        new ExcelUtil<>(BomMaster.class).exportExcel(response, list, "BOM主表数据");
    }

    /**
     * 获取BOM主表详情。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bomMasterService.selectBomMasterById(id));
    }

    /**
     * 新增BOM主表。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:add')")
    @Log(title = "BOM主表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BomMaster bomMaster) {
        if (!bomMasterService.checkBomCodeUnique(bomMaster)) {
            return error("新增BOM'" + bomMaster.getBomCode() + "'失败，BOM编码已存在");
        }
        bomMaster.setCreateBy(getUsername());
        return toAjax(bomMasterService.insertBomMaster(bomMaster));
    }

    /**
     * 修改BOM主表。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:edit')")
    @Log(title = "BOM主表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BomMaster bomMaster) {
        if (!bomMasterService.checkBomCodeUnique(bomMaster)) {
            return error("修改BOM'" + bomMaster.getBomCode() + "'失败，BOM编码已存在");
        }
        bomMaster.setUpdateBy(getUsername());
        return toAjax(bomMasterService.updateBomMaster(bomMaster));
    }

    /**
     * 删除BOM主表。
     */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:remove')")
    @Log(title = "BOM主表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bomMasterService.deleteBomMasterByIds(ids));
    }
}
