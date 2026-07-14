package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 提供给 AI 的库存余额精简数据。 */
@Data
public class InventoryBalanceToolItem {
    private String materialCode;
    private String materialName;
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
    private String lotNo;
    private String productionDate;
    private String expiryDate;
    private Integer remainingShelfLifeDays;
    private String expiryStatus;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal availableQuantity;
    private BigDecimal lockedQuantity;
    private String status;
    private String lastInboundDate;
    private String lastUpdateTime;
}
