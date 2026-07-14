package com.ruoyi.mes.purchase.domain;

import java.math.BigDecimal;
import lombok.Data;

/** 入库审核及弃审产生的库存事务流水。 */
@Data
public class InventoryTransaction {
    private Long id;
    private String transactionType;
    private Long businessId;
    private String businessCode;
    private Long businessLineId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String warehouseCode;
    private String locationCode;
    private String lotNo;
    private String productionDate;
    private String expiryDate;
    private String unit;
    private BigDecimal quantity;
    private Long sourceReceiptLineId;
    private Long sourceOrderLineId;
    private String createBy;
    private String remark;
}
