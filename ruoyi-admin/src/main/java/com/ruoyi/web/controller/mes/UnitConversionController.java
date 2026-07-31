package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mes.base.domain.UnitConversionFormula;
import com.ruoyi.mes.base.domain.Unit;
import com.ruoyi.mes.base.domain.UnitGroup;
import com.ruoyi.mes.base.domain.UnitGroupDetail;
import com.ruoyi.mes.base.dto.ConversionRequest;
import com.ruoyi.mes.base.dto.ConversionResult;
import com.ruoyi.mes.base.mapper.UnitConversionFormulaMapper;
import com.ruoyi.mes.base.mapper.UnitGroupDetailMapper;
import com.ruoyi.mes.base.mapper.UnitGroupMapper;
import com.ruoyi.mes.base.mapper.UnitMapper;
import com.ruoyi.mes.base.service.UnitConversionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 计量单位换算 Controller。
 * 提供：单位组 CRUD + 公式配置 + 核心换算 API。
 */
@RestController
@RequestMapping("/mes/base/unit")
public class UnitConversionController extends BaseController {

    private static final int DEFAULT_DECIMAL_SCALE = 4;
    private static final Set<String> VALID_ROUNDING_MODES = Set.of(
            "HALF_UP", "DOWN", "UP");

    @Autowired
    private UnitGroupMapper unitGroupMapper;
    @Autowired
    private UnitMapper unitMapper;
    @Autowired
    private UnitGroupDetailMapper detailMapper;
    @Autowired
    private UnitConversionFormulaMapper formulaMapper;
    @Autowired
    private UnitConversionService conversionService;

    // ==================== 计量单位主档 CRUD ====================

    @GetMapping("/list")
    public AjaxResult unitList(Unit unit) {
        return success(unitMapper.selectUnitList(unit));
    }

    @GetMapping("/{id}")
    public AjaxResult unitGet(@PathVariable Long id) {
        return success(unitMapper.selectUnitById(id));
    }

    @PostMapping
    public AjaxResult unitAdd(@Valid @RequestBody Unit unit) {
        unit.setCreateBy(getUsername());
        return toAjax(unitMapper.insertUnit(unit));
    }

    @PutMapping
    public AjaxResult unitEdit(@Valid @RequestBody Unit unit) {
        Unit current = unitMapper.selectUnitById(unit.getId());
        if (current == null) {
            return error("计量单位不存在");
        }
        if (!Objects.equals(current.getUnitCode(), unit.getUnitCode())
                && detailMapper.countByUnitCode(current.getUnitCode()) > 0) {
            return error("单位 " + current.getUnitCode() + " 已被单位组使用，不能修改编码");
        }
        unit.setUpdateBy(getUsername());
        return toAjax(unitMapper.updateUnit(unit));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult unitRemove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            Unit unit = unitMapper.selectUnitById(id);
            if (unit != null && detailMapper.countByUnitCode(unit.getUnitCode()) > 0) {
                return error("单位 " + unit.getUnitCode() + " 已被单位组使用，不能删除");
            }
        }
        return toAjax(unitMapper.deleteUnitByIds(ids));
    }

    // ==================== 核心换算 API ====================

    @PostMapping("/calculate")
    public AjaxResult calculate(@Valid @RequestBody ConversionRequest request) {
        Map<String, ConversionResult> results = conversionService.calculateAllUnits(request);
        return success(results);
    }

    // ==================== 单位组 CRUD ====================

    @GetMapping("/group/list")
    public AjaxResult groupList(UnitGroup unitGroup) {
        List<UnitGroup> list = unitGroupMapper.selectUnitGroupList(unitGroup);
        return success(list);
    }

    @GetMapping("/group/{id}")
    public AjaxResult groupGet(@PathVariable Long id) {
        return success(unitGroupMapper.selectUnitGroupById(id));
    }

    @PostMapping("/group")
    public AjaxResult groupAdd(@Valid @RequestBody UnitGroup unitGroup) {
        unitGroup.setCreateBy(getUsername());
        return toAjax(unitGroupMapper.insertUnitGroup(unitGroup));
    }

    @PutMapping("/group")
    public AjaxResult groupEdit(@Valid @RequestBody UnitGroup unitGroup) {
        unitGroup.setUpdateBy(getUsername());
        return toAjax(unitGroupMapper.updateUnitGroup(unitGroup));
    }

    @DeleteMapping("/group/{ids}")
    public AjaxResult groupRemove(@PathVariable Long[] ids) {
        return toAjax(unitGroupMapper.deleteUnitGroupByIds(ids));
    }

    // ==================== 单位组明细 CRUD ====================

    @GetMapping("/detail/{groupId}")
    public AjaxResult detailList(@PathVariable Long groupId) {
        return success(detailMapper.selectByGroupId(groupId));
    }

    @GetMapping("/group/code/{groupCode}/details")
    public AjaxResult detailListByGroupCode(@PathVariable String groupCode) {
        UnitGroup group = unitGroupMapper.selectUnitGroupByCode(groupCode);
        if (group == null) {
            return error("计量单位组不存在: " + groupCode);
        }
        return success(detailMapper.selectByGroupId(group.getId()));
    }

    @PostMapping("/detail")
    public AjaxResult detailAdd(@Valid @RequestBody UnitGroupDetail detail) {
        String validationMessage = normalizeDetail(detail);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        detail.setCreateBy(getUsername());
        return toAjax(detailMapper.insertDetail(detail));
    }

    @PutMapping("/detail")
    public AjaxResult detailEdit(@Valid @RequestBody UnitGroupDetail detail) {
        String validationMessage = normalizeDetail(detail);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        detail.setUpdateBy(getUsername());
        return toAjax(detailMapper.updateDetail(detail));
    }

    @DeleteMapping("/detail/{ids}")
    public AjaxResult detailRemove(@PathVariable Long[] ids) {
        return toAjax(detailMapper.deleteDetailByIds(ids));
    }

    // ==================== 公式 CRUD ====================

    @GetMapping("/formula/list")
    public AjaxResult formulaList(UnitConversionFormula formula) {
        return success(formulaMapper.selectFormulaList(formula));
    }

    @GetMapping("/formula/{id}")
    public AjaxResult formulaGet(@PathVariable Long id) {
        return success(formulaMapper.selectFormulaById(id));
    }

    @PostMapping("/formula")
    public AjaxResult formulaAdd(@Valid @RequestBody UnitConversionFormula formula) {
        String validationMessage = validateFormula(formula);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        formula.setCreateBy(getUsername());
        return toAjax(formulaMapper.insertFormula(formula));
    }

    @PutMapping("/formula")
    public AjaxResult formulaEdit(@Valid @RequestBody UnitConversionFormula formula) {
        String validationMessage = validateFormula(formula);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        formula.setUpdateBy(getUsername());
        return toAjax(formulaMapper.updateFormula(formula));
    }

    @DeleteMapping("/formula/{ids}")
    public AjaxResult formulaRemove(@PathVariable Long[] ids) {
        if (detailMapper.countByFormulaIds(ids) > 0) {
            return error("换算公式已被单位组明细使用，请先解除明细绑定");
        }
        return toAjax(formulaMapper.deleteFormulaByIds(ids));
    }

    private String validateFormula(UnitConversionFormula formula) {
        if (formula.getUnitGroupId() == null
                || unitGroupMapper.selectUnitGroupById(formula.getUnitGroupId()) == null) {
            return "计量单位组不存在";
        }
        if (unitMapper.selectUnitByCode(formula.getInputUnit()) == null) {
            return "源单位不存在，请先维护单位主档";
        }
        if (unitMapper.selectUnitByCode(formula.getOutputUnit()) == null) {
            return "目标单位不存在，请先维护单位主档";
        }
        if (Objects.equals(formula.getInputUnit(), formula.getOutputUnit())) {
            return "源单位和目标单位不能相同";
        }
        if (detailMapper.selectByGroupAndUnit(formula.getUnitGroupId(), formula.getInputUnit()) == null) {
            return "源单位不属于当前计量单位组";
        }
        if (detailMapper.selectByGroupAndUnit(formula.getUnitGroupId(), formula.getOutputUnit()) == null) {
            return "目标单位不属于当前计量单位组";
        }
        if (formula.getDecimalScale() == null) {
            formula.setDecimalScale(DEFAULT_DECIMAL_SCALE);
        }
        if (formula.getDecimalScale() < 0 || formula.getDecimalScale() > 6) {
            return "小数位数必须在0到6之间";
        }
        if (formula.getRoundingMode() == null || formula.getRoundingMode().isBlank()) {
            formula.setRoundingMode("HALF_UP");
        } else {
            formula.setRoundingMode(formula.getRoundingMode().toUpperCase());
        }
        if (!VALID_ROUNDING_MODES.contains(formula.getRoundingMode())) {
            return "舍入模式不受支持";
        }
        return null;
    }

    private String normalizeDetail(UnitGroupDetail detail) {
        if (detail.getGroupId() == null || unitGroupMapper.selectUnitGroupById(detail.getGroupId()) == null) {
            return "计量单位组不存在";
        }
        Unit unit = unitMapper.selectUnitByCode(detail.getUnitCode());
        if (unit == null) {
            return "计量单位不存在，请先维护单位主档";
        }
        detail.setUnitName(unit.getUnitName());
        if (detail.getFormulaId() == null) {
            return null;
        }
        UnitConversionFormula formula = formulaMapper.selectFormulaById(detail.getFormulaId());
        if (formula == null) {
            return "换算公式不存在";
        }
        if (!Objects.equals(formula.getUnitGroupId(), detail.getGroupId())) {
            return "换算公式不属于当前计量单位组";
        }
        if (!Objects.equals(formula.getInputUnit(), detail.getUnitCode())) {
            return "换算公式源单位必须与明细单位一致";
        }
        if (detailMapper.selectByGroupAndUnit(detail.getGroupId(), formula.getOutputUnit()) == null) {
            return "换算公式目标单位必须属于当前计量单位组";
        }
        return null;
    }
}
