package com.ruoyi.mes.purchase.domain;
import com.ruoyi.common.core.domain.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true) public class PurchaseReceiptLine extends BaseEntity { private Long id,receiptId,orderLineId,materialId; private Integer lineNo; private String materialCode,materialName,spec,unit,lotNo,productionDate,expiryDate,warehouseCode,warehouseName,locationCode,locationName; private BigDecimal receivedQuantity,qualifiedQuantity,rejectedQuantity,pendingQuantity; }
