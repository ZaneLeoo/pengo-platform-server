package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 可参照生成送货单的采购订单明细。 */
@Data
public class ReceiptReferenceLine {
    private Long sourceOrderId;
    private String sourceOrderCode;
    private Long sourceOrderLineId;
    private Integer sourceOrderLineNo;
    private String supplierCode;
    private String supplierName;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private String lotControlFlag;
    private String shelfLifeControlFlag;
    private Integer shelfLifeDays;
    private Integer expiryWarningDays;
    private BigDecimal orderQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal remainingQuantity;
}
