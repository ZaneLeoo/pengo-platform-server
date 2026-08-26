package com.ruoyi.mes.base.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.UnitConversionFormula;
import com.ruoyi.mes.base.domain.UnitGroup;
import com.ruoyi.mes.base.domain.UnitGroupDetail;
import com.ruoyi.mes.base.dto.ConversionRequest;
import com.ruoyi.mes.base.dto.ConversionResult;
import com.ruoyi.mes.base.engine.FormulaEngine;
import com.ruoyi.mes.base.mapper.MaterialMapper;
import com.ruoyi.mes.base.mapper.UnitGroupDetailMapper;
import com.ruoyi.mes.base.mapper.UnitGroupMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UnitConversionServiceTest {

    @Test
    void calculatesAllGroupUnitsWithoutInventoryBaseUnit() {
        MaterialMapper materialMapper = mock(MaterialMapper.class);
        UnitGroupMapper unitGroupMapper = mock(UnitGroupMapper.class);
        UnitGroupDetailMapper detailMapper = mock(UnitGroupDetailMapper.class);
        FormulaEngine formulaEngine = mock(FormulaEngine.class);

        Material material = new Material();
        material.setMaterialId(31L);
        material.setMaterialCode("TAPE-001");
        material.setUnit(null);
        material.setUnitGroupCode("SQ_ROLL_BOX");

        UnitGroup group = new UnitGroup();
        group.setId(1L);
        group.setGroupCode("SQ_ROLL_BOX");

        UnitGroupDetail square = detail("SQ", "平方米", null);
        UnitGroupDetail roll = detail("ROL", "卷", 101L);
        UnitGroupDetail box = detail("BOX", "箱", 102L);

        UnitConversionFormula rollToSquare = formula(101L, "ROL", "SQ");
        UnitConversionFormula boxToSquare = formula(102L, "BOX", "SQ");

        when(materialMapper.selectMaterialById(31L)).thenReturn(material);
        when(unitGroupMapper.selectUnitGroupByCode("SQ_ROLL_BOX")).thenReturn(group);
        when(detailMapper.selectByGroupId(1L)).thenReturn(List.of(square, roll, box));
        when(formulaEngine.findFormula(eq(1L), same(material), eq("ROL"), eq("SQ"), eq(101L)))
                .thenReturn(rollToSquare);
        when(formulaEngine.findFormula(eq(1L), same(material), eq("BOX"), eq("SQ"), eq(102L)))
                .thenReturn(boxToSquare);
        when(formulaEngine.reverseEvaluate(
                        eq(rollToSquare),
                        same(material),
                        any(BigDecimal.class),
                        nullable(Map.class)))
                .thenAnswer(
                        invocation ->
                                invocation
                                        .getArgument(2, BigDecimal.class)
                                        .divide(new BigDecimal("10")));
        when(formulaEngine.reverseEvaluate(
                        eq(boxToSquare),
                        same(material),
                        any(BigDecimal.class),
                        nullable(Map.class)))
                .thenAnswer(
                        invocation ->
                                invocation
                                        .getArgument(2, BigDecimal.class)
                                        .divide(new BigDecimal("20")));

        UnitConversionService service = new UnitConversionService();
        ReflectionTestUtils.setField(service, "materialMapper", materialMapper);
        ReflectionTestUtils.setField(service, "unitGroupMapper", unitGroupMapper);
        ReflectionTestUtils.setField(service, "detailMapper", detailMapper);
        ReflectionTestUtils.setField(service, "formulaEngine", formulaEngine);

        ConversionRequest request = new ConversionRequest();
        request.setMaterialId(31L);
        request.setInputUnitCode("SQ");
        request.setInputQuantity(new BigDecimal("100"));

        Map<String, ConversionResult> results = service.calculateAllUnits(request);

        assertEquals(new BigDecimal("100"), results.get("SQ").getQuantity());
        assertEquals(new BigDecimal("10"), results.get("ROL").getQuantity());
        assertEquals(new BigDecimal("5"), results.get("BOX").getQuantity());
    }

    private static UnitGroupDetail detail(String code, String name, Long formulaId) {
        UnitGroupDetail detail = new UnitGroupDetail();
        detail.setUnitCode(code);
        detail.setUnitName(name);
        detail.setFormulaId(formulaId);
        return detail;
    }

    private static UnitConversionFormula formula(Long id, String inputUnit, String outputUnit) {
        UnitConversionFormula formula = new UnitConversionFormula();
        formula.setId(id);
        formula.setUnitGroupId(1L);
        formula.setInputUnit(inputUnit);
        formula.setOutputUnit(outputUnit);
        formula.setReverseMode("DIVIDE");
        return formula;
    }
}
