package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.ISupplierService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mes/base/supplier")
public class SupplierController extends BaseController {

    private final ISupplierService supplierService;

    public SupplierController(ISupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PreAuthorize("@ss.hasPermi('base:supplier:list')")
    @GetMapping("/list")
    public TableDataInfo list(Supplier query) {
        startPage();
        return getDataTable(supplierService.selectList(query));
    }

    @PreAuthorize("@ss.hasPermi('base:supplier:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(supplierService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('base:supplier:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Supplier supplier) {
        supplier.setCreateBy(getUsername());
        return toAjax(supplierService.insert(supplier));
    }

    @PreAuthorize("@ss.hasPermi('base:supplier:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Supplier supplier) {
        supplier.setUpdateBy(getUsername());
        return toAjax(supplierService.update(supplier));
    }

    @PreAuthorize("@ss.hasPermi('base:supplier:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(supplierService.deleteByIds(ids));
    }
}
