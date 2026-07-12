package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mes.base.domain.Location;
import com.ruoyi.mes.base.service.ILocationService;
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
@RequestMapping("/mes/base/location")
public class LocationController extends BaseController {

    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("@ss.hasPermi('base:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(Location query) {
        startPage();
        return getDataTable(locationService.selectList(query));
    }

    @PreAuthorize("@ss.hasPermi('base:location:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(locationService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('base:location:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Location location) {
        location.setCreateBy(getUsername());
        return toAjax(locationService.insert(location));
    }

    @PreAuthorize("@ss.hasPermi('base:location:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Location location) {
        location.setUpdateBy(getUsername());
        return toAjax(locationService.update(location));
    }

    @PreAuthorize("@ss.hasPermi('base:location:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(locationService.deleteByIds(ids));
    }
}
