package com.ruoyi.mes.purchase.domain;
import com.ruoyi.common.core.domain.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true) public class InventoryBalance extends BaseEntity { private Long id,materialId; private String materialCode,materialName,warehouseCode,warehouseName,locationCode,locationName,lotNo,unit,status,lastInboundDate,lastUpdateTime; private BigDecimal quantity,availableQuantity,lockedQuantity; }
