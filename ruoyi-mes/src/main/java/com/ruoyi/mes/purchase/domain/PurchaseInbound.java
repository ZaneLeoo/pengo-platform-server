package com.ruoyi.mes.purchase.domain;
import com.ruoyi.common.core.domain.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal; import java.util.List;
@Data @EqualsAndHashCode(callSuper=true) public class PurchaseInbound extends BaseEntity { private Long id,receiptId; private String inboundCode,receiptCode,inboundDate,warehouseCode,warehouseName,status; private BigDecimal totalQuantity; private List<PurchaseInboundLine> lines; }
