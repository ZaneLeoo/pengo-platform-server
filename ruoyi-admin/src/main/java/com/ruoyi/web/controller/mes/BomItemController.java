package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.service.IBomItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * BOM子件明细控制器。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mes/base/bomItem")
public class BomItemController extends BaseController {
    @Autowired
    private IBomItemService bomItemService;

    /**
     * 查询BOM子件明细列表。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(BomItem bomItem) {
        startPage();
        return getDataTable(bomItemService.selectBomItemList(bomItem));
    }

    /**
     * 获取BOM子件明细详情。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bomItemService.selectBomItemById(id));
    }

    /**
     * 新增BOM子件明细。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:add')")
    @Log(title = "BOM子件明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BomItem bomItem) {
        if (!bomItemService.checkLineNoUnique(bomItem)) {
            return error("新增BOM明细失败，行号已存在");
        }
        bomItem.setCreateBy(getUsername());
        return toAjax(bomItemService.insertBomItem(bomItem));
    }

    /**
     * 修改BOM子件明细。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:edit')")
    @Log(title = "BOM子件明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BomItem bomItem) {
        if (!bomItemService.checkLineNoUnique(bomItem)) {
            return error("修改BOM明细失败，行号已存在");
        }
        bomItem.setUpdateBy(getUsername());
        return toAjax(bomItemService.updateBomItem(bomItem));
    }

    /**
     * 删除BOM子件明细。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:remove')")
    @Log(title = "BOM子件明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bomItemService.deleteBomItemByIds(ids));
    }

    /**
     * 懒加载 BOM 树形子节点（按父件编码查询）。
     * parentItemCode 为空/不传时返回顶层子件。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:list')")
    @GetMapping("/children")
    public AjaxResult children(@RequestParam Long bomVersionId,
                               @RequestParam(required = false) String parentItemCode) {
        return success(bomItemService.selectBomItemChildren(bomVersionId,
            parentItemCode == null || parentItemCode.isBlank() ? null : parentItemCode));
    }

    /**
     * 跨BOM懒加载：按子件编码查找其BOM版本下的子件列表。
     * versionId传入则取指定版本，为空则取默认版本。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomItem:list')")
    @GetMapping("/childrenByComponent")
    public AjaxResult childrenByComponent(@RequestParam String componentItemCode,
                                          @RequestParam(required = false) Long bomVersionId) {
        return success(bomItemService.selectBomItemByComponentCode(componentItemCode, bomVersionId));
    }
}
