package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.mes.base.domain.UnitGroupDetail;
import com.ruoyi.mes.base.dto.ConversionResult;
import com.ruoyi.mes.base.service.UnitConversionService;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseOrderServiceImplTest {

    @Test
    void savesPurchaseLineUsingInputUnitAndInputQuantity() {
        PurchaseOrderMapper orderMapper = mock(PurchaseOrderMapper.class);
        UnitConversionService conversionService = mock(UnitConversionService.class);
        PurchaseOrderServiceImpl service = new PurchaseOrderServiceImpl(orderMapper, conversionService);

        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setLineNo(1);
        line.setMaterialId(31L);
        line.setMaterialCode("TAPE-001");
        line.setMaterialName("电子胶带");
        line.setUnit("SQ");
        line.setOrderQuantity(new BigDecimal("999"));
        line.setInputUnitCode("ROL");
        line.setInputQty(new BigDecimal("2"));
        line.setUnitPrice(new BigDecimal("12.50"));

        List<UnitGroupDetail> details = List.of(
                detail("SQ", "平方米"),
                detail("ROL", "卷"),
                detail("BOX", "箱"));
        Map<String, ConversionResult> results = new LinkedHashMap<>();
        results.put("SQ", result("SQ", "平方米", "157.4"));
        results.put("ROL", result("ROL", "卷", "2"));
        results.put("BOX", result("BOX", "箱", "0.5"));

        when(conversionService.getUnitDetails(31L)).thenReturn(details);
        when(conversionService.calculateAllUnits(org.mockito.ArgumentMatchers.any()))
                .thenReturn(results);

        PurchaseOrder order = new PurchaseOrder();
        order.setLines(List.of(line));

        service.insertPurchaseOrder(order);

        assertEquals("ROL", line.getUnit());
        assertEquals(0, line.getOrderQuantity().compareTo(new BigDecimal("2.000000")));
        assertEquals("SQ", line.getUnit1Code());
        assertEquals(0, line.getUnit1Qty().compareTo(new BigDecimal("157.400000")));
        assertEquals("ROL", line.getUnit2Code());
        assertEquals(0, line.getUnit2Qty().compareTo(new BigDecimal("2.000000")));
        assertEquals("BOX", line.getUnit3Code());
        assertEquals(0, line.getUnit3Qty().compareTo(new BigDecimal("0.500000")));
        assertEquals(0, line.getAmount().compareTo(new BigDecimal("25.00")));
        assertEquals(0, order.getTotalQuantity().compareTo(new BigDecimal("2.000000")));
        assertEquals(0, order.getTotalAmount().compareTo(new BigDecimal("25.00")));
        verify(orderMapper).insertPurchaseOrderLine(line);
    }

    @Test
    void savesPurchaseLineWithTwoUnitGroupMembers() {
        PurchaseOrderMapper orderMapper = mock(PurchaseOrderMapper.class);
        UnitConversionService conversionService = mock(UnitConversionService.class);
        PurchaseOrderServiceImpl service = new PurchaseOrderServiceImpl(orderMapper, conversionService);

        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setLineNo(1);
        line.setMaterialId(32L);
        line.setMaterialCode("FABRIC-001");
        line.setMaterialName("面料");
        line.setInputUnitCode("M");
        line.setInputQty(new BigDecimal("5"));
        line.setUnit3Code("OLD");
        line.setUnit3Name("旧单位");
        line.setUnit3Qty(new BigDecimal("99"));
        line.setUnitPrice(new BigDecimal("8"));

        List<UnitGroupDetail> details = List.of(
                detail("M", "米"),
                detail("ROLL", "卷"));
        Map<String, ConversionResult> results = new LinkedHashMap<>();
        results.put("M", result("M", "米", "5"));
        results.put("ROLL", result("ROLL", "卷", "0.5"));

        when(conversionService.getUnitDetails(32L)).thenReturn(details);
        when(conversionService.calculateAllUnits(org.mockito.ArgumentMatchers.any()))
                .thenReturn(results);

        PurchaseOrder order = new PurchaseOrder();
        order.setLines(List.of(line));

        service.insertPurchaseOrder(order);

        assertEquals("M", line.getUnit1Code());
        assertEquals("ROLL", line.getUnit2Code());
        assertEquals(0, line.getUnit2Qty().compareTo(new BigDecimal("0.500000")));
        assertNull(line.getUnit3Code());
        assertNull(line.getUnit3Name());
        assertNull(line.getUnit3Qty());
        verify(orderMapper).insertPurchaseOrderLine(line);
    }

    private static UnitGroupDetail detail(String code, String name) {
        UnitGroupDetail detail = new UnitGroupDetail();
        detail.setUnitCode(code);
        detail.setUnitName(name);
        return detail;
    }

    private static ConversionResult result(String code, String name, String quantity) {
        ConversionResult result = new ConversionResult();
        result.setUnitCode(code);
        result.setUnitName(name);
        result.setQuantity(new BigDecimal(quantity));
        return result;
    }
}
