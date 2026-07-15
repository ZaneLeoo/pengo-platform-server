package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.mapper.PurchaseInboundMapper;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 统一保存采购到货、入库草稿，供人工界面和自动化入口复用。 */
@Service
public class PurchaseDocumentDraftService {
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseInboundMapper inboundMapper;
    private final ShelfLifeService shelfLifeService;

    public PurchaseDocumentDraftService(PurchaseReceiptMapper receiptMapper, PurchaseInboundMapper inboundMapper,
            ShelfLifeService shelfLifeService) {
        this.receiptMapper = receiptMapper;
        this.inboundMapper = inboundMapper;
        this.shelfLifeService = shelfLifeService;
    }

    /** 保存采购到货草稿及全部明细。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createReceiptDraft(PurchaseReceipt receipt, String operator) {
        receipt.setStatus(PurchaseDocumentStatus.DRAFT.getCode());
        receipt.setCreateBy(operator);
        receiptMapper.insert(receipt);
        receipt.getLines().forEach(line -> {
            shelfLifeService.prepareReceiptLine(line);
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
        inbound.getLines().forEach(line -> {
            shelfLifeService.validateInboundLine(line);
            line.setInboundId(inbound.getId());
            line.setCreateBy(operator);
            inboundMapper.insertLine(line);
        });
        return inbound.getId();
    }
}
