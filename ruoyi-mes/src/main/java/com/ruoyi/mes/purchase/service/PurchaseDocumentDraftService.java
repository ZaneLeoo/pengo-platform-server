package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.mapper.PurchaseFlowMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseInboundMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 统一保存采购到货、入库草稿，供人工界面和自动化入口复用。 */
@Service
public class PurchaseDocumentDraftService {
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseInboundMapper inboundMapper;
    private final ShelfLifeService shelfLifeService;
    private final PurchaseFlowMapper flowMapper;

    public PurchaseDocumentDraftService(
            PurchaseReceiptMapper receiptMapper,
            PurchaseInboundMapper inboundMapper,
            ShelfLifeService shelfLifeService,
            PurchaseFlowMapper flowMapper) {
        this.receiptMapper = receiptMapper;
        this.inboundMapper = inboundMapper;
        this.shelfLifeService = shelfLifeService;
        this.flowMapper = flowMapper;
    }

    /** 保存采购到货草稿及全部明细。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createReceiptDraft(PurchaseReceipt receipt, String operator) {
        receipt.setStatus(PurchaseDocumentStatus.DRAFT.getCode());
        receipt.setCreateBy(operator);
        receiptMapper.insert(receipt);
        receipt.getLines()
                .forEach(
                        line -> {
                            var source =
                                    flowMapper
                                            .selectReceiptReferenceLines(null, null, null)
                                            .stream()
                                            .filter(
                                                    item ->
                                                            item.getSourceOrderLineId()
                                                                    .equals(
                                                                            line
                                                                                    .getSourceOrderLineId()))
                                            .findFirst()
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalArgumentException(
                                                                    "来源采购订单明细无效"));
                            applyReceiptSource(line, source);
                            shelfLifeService.prepareReceiptLine(line);
                            line.setReceiptAmount(
                                    line.getReceivedQuantity()
                                            .multiply(line.getSourceUnitPrice())
                                            .setScale(2, RoundingMode.HALF_UP));
                            line.setReceiptId(receipt.getId());
                            line.setCreateBy(operator);
                            receiptMapper.insertLine(line);
                        });
        return receipt.getId();
    }

    /** 保存采购入库草稿及全部明细。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createInboundDraft(PurchaseInbound inbound, String operator) {
        inbound.setStatus(PurchaseDocumentStatus.DRAFT.getCode());
        inbound.setCreateBy(operator);
        inboundMapper.insert(inbound);
        inbound.getLines()
                .forEach(
                        line -> {
                            var source =
                                    flowMapper
                                            .selectInboundReferenceLines(null, null, null)
                                            .stream()
                                            .filter(
                                                    item ->
                                                            item.getSourceReceiptLineId()
                                                                    .equals(
                                                                            line
                                                                                    .getSourceReceiptLineId()))
                                            .findFirst()
                                            .orElseThrow(
                                                    () -> new IllegalArgumentException("来源到货明细无效"));
                            applyInboundSource(line, source);
                            shelfLifeService.validateInboundLine(line);
                            line.setInboundAmount(
                                    line.getInboundQuantity()
                                            .multiply(line.getSourceUnitPrice())
                                            .setScale(2, RoundingMode.HALF_UP));
                            line.setInboundId(inbound.getId());
                            line.setCreateBy(operator);
                            inboundMapper.insertLine(line);
                        });
        return inbound.getId();
    }

    private void applyReceiptSource(
            com.ruoyi.mes.purchase.domain.PurchaseReceiptLine line,
            com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine source) {
        line.setProjectId(source.getProjectId());
        line.setProjectCode(source.getProjectCode());
        line.setProjectName(source.getProjectName());
        line.setCostCategoryId(source.getCostCategoryId());
        line.setCategoryCode(source.getCategoryCode());
        line.setCategoryName(source.getCategoryName());
        line.setCategoryPath(source.getCategoryPath());
        line.setSourceUnitPrice(source.getSourceUnitPrice());
    }

    private void applyInboundSource(
            com.ruoyi.mes.purchase.domain.PurchaseInboundLine line,
            com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine source) {
        line.setProjectId(source.getProjectId());
        line.setProjectCode(source.getProjectCode());
        line.setProjectName(source.getProjectName());
        line.setCostCategoryId(source.getCostCategoryId());
        line.setCategoryCode(source.getCategoryCode());
        line.setCategoryName(source.getCategoryName());
        line.setCategoryPath(source.getCategoryPath());
        line.setSourceUnitPrice(source.getSourceUnitPrice());
    }
}
