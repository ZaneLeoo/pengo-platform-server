package com.ruoyi.mes.purchase.domain;
import com.ruoyi.common.core.domain.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal; import java.util.List;
@Data @EqualsAndHashCode(callSuper=true) public class PurchaseReceipt extends BaseEntity { private Long id; private String receiptCode,orderCode,supplierCode,supplierName,receiptDate,status,inspectionStatus; private Long orderId; private BigDecimal totalQuantity; private List<PurchaseReceiptLine> lines; }
