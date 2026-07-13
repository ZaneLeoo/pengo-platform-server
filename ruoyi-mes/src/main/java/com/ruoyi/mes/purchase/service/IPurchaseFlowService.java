package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.InspectionRequest;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import java.util.List;

/** 采购、送货、入库的状态流转服务。 */
public interface IPurchaseFlowService {
    void approveOrder(Long id, String operator);
    void unapproveOrder(Long id, String operator);
    void approveReceipt(Long id, String operator);
    void unapproveReceipt(Long id, String operator);
    void inspectReceipt(Long id, InspectionRequest request, String operator);
    void uninspectReceipt(Long id, String operator);
    void approveInbound(Long id, String operator);
    void unapproveInbound(Long id, String operator);
    List<ReceiptReferenceLine> selectReceiptReferenceLines(String orderCode, String supplierName, String materialCode);
    List<InboundReferenceLine> selectInboundReferenceLines(String receiptCode, String warehouseCode,
            String materialCode);
}
