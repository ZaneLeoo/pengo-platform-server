package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mes.base.domain.Unit;
import com.ruoyi.mes.base.mapper.UnitMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 单一计量单位主数据。多计量单位换算功能不在此控制器内提供。 */
@RestController
@RequestMapping("/mes/base/unit")
public class UnitController extends BaseController {
    private final UnitMapper unitMapper;

    public UnitController(UnitMapper unitMapper) {
        this.unitMapper = unitMapper;
    }

    @GetMapping("/list")
    public AjaxResult list(Unit unit) {
        return success(unitMapper.selectUnitList(unit));
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(unitMapper.selectUnitById(id));
    }

    @PostMapping
    public AjaxResult add(@Valid @RequestBody Unit unit) {
        unit.setCreateBy(getUsername());
        return toAjax(unitMapper.insertUnit(unit));
    }

    @PutMapping
    public AjaxResult edit(@Valid @RequestBody Unit unit) {
        if (unitMapper.selectUnitById(unit.getId()) == null) {
            return error("计量单位不存在");
        }
        unit.setUpdateBy(getUsername());
        return toAjax(unitMapper.updateUnit(unit));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(unitMapper.deleteUnitByIds(ids));
    }
}
