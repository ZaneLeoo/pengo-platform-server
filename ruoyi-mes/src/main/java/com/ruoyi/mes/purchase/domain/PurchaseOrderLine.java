package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购订单明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderLine extends BaseEntity
{
    private Long id;
    private Long orderId;
    private Integer lineNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String spec;
    private String model;
    private String unit;
    private BigDecimal orderQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal qualifiedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private String plannedDate;
}
