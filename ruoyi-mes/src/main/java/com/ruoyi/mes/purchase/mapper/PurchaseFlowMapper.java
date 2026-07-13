package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.InventoryTransaction;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 采购、送货、入库流转的数据操作。 */
public interface PurchaseFlowMapper {
    int updateOrderStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("operator") String operator);
    int updateReceiptStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("operator") String operator);
    int updateInboundStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("operator") String operator);
    int countApprovedReceiptByOrder(@Param("orderId") Long orderId);
    int countApprovedInboundByReceipt(@Param("receiptId") Long receiptId);
    int countReceiptSourceSuppliers(@Param("receiptId") Long receiptId);
    int countReceiptHeaderSupplierMismatch(@Param("receiptId") Long receiptId);
    int countValidReceiptSource(@Param("line") PurchaseReceiptLine line);
    int countValidInboundSource(@Param("line") PurchaseInboundLine line);
    int increaseOrderReceived(@Param("orderLineId") Long orderLineId, @Param("quantity") BigDecimal quantity);
    int increaseOrderQualified(@Param("orderLineId") Long orderLineId, @Param("quantity") BigDecimal quantity);
    int increaseOrderInbound(@Param("orderLineId") Long orderLineId, @Param("quantity") BigDecimal quantity);
    int resetReceiptInspection(@Param("receiptId") Long receiptId);
    int updateReceiptInspection(@Param("line") PurchaseReceiptLine line);
    int updateReceiptInspectionStatus(@Param("receiptId") Long receiptId, @Param("status") String status,
            @Param("operator") String operator);
    int increaseReceiptInbound(@Param("receiptLineId") Long receiptLineId, @Param("quantity") BigDecimal quantity);
    int upsertInventoryBalance(@Param("transaction") InventoryTransaction transaction);
    int decreaseInventoryBalance(@Param("transaction") InventoryTransaction transaction);
    int insertInventoryTransaction(InventoryTransaction transaction);
    List<ReceiptReferenceLine> selectReceiptReferenceLines(@Param("orderCode") String orderCode,
            @Param("supplierName") String supplierName,
            @Param("materialCode") String materialCode);

    List<InboundReferenceLine> selectInboundReferenceLines(@Param("receiptCode") String receiptCode,
            @Param("warehouseCode") String warehouseCode,
            @Param("materialCode") String materialCode);
}
