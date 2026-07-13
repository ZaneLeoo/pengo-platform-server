package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/** 提供给 AI 的库存流水精简数据。 */
@Data
public class InventoryTransactionToolItem {
    private String transactionType;
    private String transactionTypeLabel;
    private String businessCode;
    private String materialCode;
    private String materialName;
    private String warehouseCode;
    private String locationCode;
    private String lotNo;
    private String unit;
    private BigDecimal quantity;
    private Date occurredTime;
    private String sourceReceiptCode;
    private String sourceOrderCode;
}
