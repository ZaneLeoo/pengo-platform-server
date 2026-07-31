package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.mes.base.domain.UnitGroupDetail;
import com.ruoyi.mes.base.dto.ConversionRequest;
import com.ruoyi.mes.base.dto.ConversionResult;
import com.ruoyi.mes.base.service.UnitConversionService;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.mapper.PurchaseOrderMapper;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购订单业务处理。 */
@Service
public class PurchaseOrderServiceImpl implements IPurchaseOrderService {
    private final PurchaseOrderMapper orderMapper;
    private final UnitConversionService conversionService;

    public PurchaseOrderServiceImpl(PurchaseOrderMapper orderMapper,
                                    UnitConversionService conversionService) {
        this.orderMapper = orderMapper;
        this.conversionService = conversionService;
    }

    @Override
    public List<PurchaseOrder> selectPurchaseOrderList(PurchaseOrder order) {
        return orderMapper.selectPurchaseOrderList(order);
    }

    @Override
    public PurchaseOrder selectPurchaseOrderById(Long id) {
        PurchaseOrder order = orderMapper.selectPurchaseOrderById(id);
        if (order != null)
            order.setLines(orderMapper.selectPurchaseOrderLineList(id));
        return order;
    }

    @Override
    public boolean checkOrderCodeUnique(PurchaseOrder order) {
        PurchaseOrder existing = orderMapper.selectPurchaseOrderByCode(order.getOrderCode());
        return existing == null || existing.getId().equals(order.getId());
    }

    @Override
    @Transactional
    public int insertPurchaseOrder(PurchaseOrder order) {
        prepareOrder(order);
        orderMapper.insertPurchaseOrder(order);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int updatePurchaseOrder(PurchaseOrder order) {
        prepareOrder(order);
        orderMapper.updatePurchaseOrder(order);
        Long[] ids = {order.getId()};
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        insertLines(order);
        return 1;
    }

    @Override
    @Transactional
    public int deletePurchaseOrderByIds(Long[] ids) {
        orderMapper.deletePurchaseOrderLineByOrderIds(ids);
        return orderMapper.deletePurchaseOrderByIds(ids);
    }

    private void insertLines(PurchaseOrder order) {
        if (order.getLines() == null)
            return;
        for (PurchaseOrderLine line : order.getLines()) {
            line.setOrderId(order.getId());
            if (line.getLineNo() == null)
                line.setLineNo(order.getLines().indexOf(line) + 1);
            if (line.getReceivedQuantity() == null)
                line.setReceivedQuantity(java.math.BigDecimal.ZERO);
            if (line.getQualifiedQuantity() == null)
                line.setQualifiedQuantity(java.math.BigDecimal.ZERO);
            if (line.getInboundQuantity() == null)
                line.setInboundQuantity(java.math.BigDecimal.ZERO);
            if (com.ruoyi.common.utils.StringUtils.isBlank(line.getPriceSource()))
                line.setPriceSource("MANUAL");
            orderMapper.insertPurchaseOrderLine(line);
        }
    }

    /**
     * 重新计算订单行并汇总订单金额。
     * <p>采购行按录入单位计价，unit/orderQuantity 仅作为旧表结构兼容字段。</p>
     */
    private void prepareOrder(PurchaseOrder order) {
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new IllegalArgumentException("采购订单至少需要一条明细");
        }
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderLine line : order.getLines()) {
            normalizeLineUnits(line);
            BigDecimal unitPrice = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("采购单价不能小于0: " + line.getMaterialCode());
            }
            line.setUnitPrice(unitPrice);
            BigDecimal amount = line.getInputQty().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            line.setAmount(amount);
            totalQuantity = totalQuantity.add(line.getInputQty());
            totalAmount = totalAmount.add(amount);
        }
        order.setTotalQuantity(scale(totalQuantity, 6));
        order.setTotalAmount(scale(totalAmount, 2));
    }

    /**
     * 以录入单位为入口计算单位组内三个对称单位的数量。
     * 前端结果只用于即时展示，最终保存统一由服务端重新计算。
     */
    private void normalizeLineUnits(PurchaseOrderLine line) {
        List<UnitGroupDetail> details = conversionService.getUnitDetails(line.getMaterialId());
        if (details.size() != 3) {
            throw new IllegalArgumentException("采购多计量换算要求物料单位组配置恰好三个单位: "
                    + line.getMaterialCode());
        }

        String inputUnitCode = resolveUnitCode(details, line.getInputUnitCode());
        if (inputUnitCode == null) {
            inputUnitCode = resolveUnitCode(details, line.getUnit());
        }
        if (inputUnitCode == null) {
            throw new IllegalArgumentException("采购明细录入单位不能为空: " + line.getMaterialCode());
        }

        BigDecimal inputQuantity = line.getInputQty();
        if (inputQuantity == null) {
            inputQuantity = line.getOrderQuantity();
        }
        if (inputQuantity == null || inputQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("采购明细输入数量必须大于0: " + line.getMaterialCode());
        }

        ConversionRequest request = new ConversionRequest();
        request.setMaterialId(line.getMaterialId());
        request.setInputUnitCode(inputUnitCode);
        request.setInputQuantity(inputQuantity);
        Map<String, ConversionResult> results = conversionService.calculateAllUnits(request);

        line.setInputUnitCode(inputUnitCode);
        line.setInputUnitName(findDetailName(details, inputUnitCode));
        line.setInputQty(scale(inputQuantity, 6));
        line.setUnit(inputUnitCode);
        line.setOrderQuantity(line.getInputQty());

        applyUnitSnapshot(line, details.get(0), results, 1);
        applyUnitSnapshot(line, details.get(1), results, 2);
        applyUnitSnapshot(line, details.get(2), results, 3);
    }

    private void applyUnitSnapshot(PurchaseOrderLine line, UnitGroupDetail detail,
                                   Map<String, ConversionResult> results, int index) {
        ConversionResult result = requiredResult(results, detail, line);
        if (index == 1) {
            line.setUnit1Code(detail.getUnitCode());
            line.setUnit1Name(detail.getUnitName());
            line.setUnit1Qty(scale(result.getQuantity(), 6));
        } else if (index == 2) {
            line.setUnit2Code(detail.getUnitCode());
            line.setUnit2Name(detail.getUnitName());
            line.setUnit2Qty(scale(result.getQuantity(), 6));
        } else {
            line.setUnit3Code(detail.getUnitCode());
            line.setUnit3Name(detail.getUnitName());
            line.setUnit3Qty(scale(result.getQuantity(), 6));
        }
    }

    private ConversionResult requiredResult(Map<String, ConversionResult> results,
                                            UnitGroupDetail detail, PurchaseOrderLine line) {
        ConversionResult result = results.get(detail.getUnitCode());
        if (result == null || result.getQuantity() == null
                || result.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("物料 " + line.getMaterialCode()
                    + " 缺少有效的 " + detail.getUnitName() + " 换算公式");
        }
        return result;
    }

    private String resolveUnitCode(List<UnitGroupDetail> details, String value) {
        if (com.ruoyi.common.utils.StringUtils.isBlank(value)) return null;
        return details.stream()
                .filter(detail -> value.equals(detail.getUnitCode()) || value.equals(detail.getUnitName()))
                .map(UnitGroupDetail::getUnitCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("输入单位不在物料计量单位组中: " + value));
    }

    private String findDetailName(List<UnitGroupDetail> details, String unitCode) {
        return details.stream()
                .filter(detail -> unitCode.equals(detail.getUnitCode()))
                .map(UnitGroupDetail::getUnitName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("单位不存在: " + unitCode));
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
