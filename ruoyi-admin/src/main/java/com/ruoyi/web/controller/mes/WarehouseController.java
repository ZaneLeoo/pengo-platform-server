package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mes.base.domain.Warehouse;
import com.ruoyi.mes.base.service.IWarehouseService;
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
@RequestMapping("/mes/base/warehouse")
public class WarehouseController extends BaseController {

    private final IWarehouseService warehouseService;

    public WarehouseController(IWarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PreAuthorize("@ss.hasPermi('mes:warehouse:list')")
    @GetMapping("/list")
    public TableDataInfo list(Warehouse query) {
        startPage();
        return getDataTable(warehouseService.selectList(query));
    }

    @PreAuthorize("@ss.hasPermi('mes:warehouse:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(warehouseService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('mes:warehouse:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Warehouse warehouse) {
        warehouse.setCreateBy(getUsername());
        return toAjax(warehouseService.insert(warehouse));
    }

    @PreAuthorize("@ss.hasPermi('mes:warehouse:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Warehouse warehouse) {
        warehouse.setUpdateBy(getUsername());
        return toAjax(warehouseService.update(warehouse));
    }

    @PreAuthorize("@ss.hasPermi('mes:warehouse:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(warehouseService.deleteByIds(ids));
    }
}
