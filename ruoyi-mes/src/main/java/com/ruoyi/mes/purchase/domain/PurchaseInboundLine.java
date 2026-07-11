package com.ruoyi.mes.purchase.domain;
import com.ruoyi.common.core.domain.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true) public class PurchaseInboundLine extends BaseEntity { private Long id,inboundId,receiptLineId,materialId; private Integer lineNo; private String materialCode,materialName,spec,unit,lotNo,warehouseCode,warehouseName,locationCode,locationName; private BigDecimal inboundQuantity; }
