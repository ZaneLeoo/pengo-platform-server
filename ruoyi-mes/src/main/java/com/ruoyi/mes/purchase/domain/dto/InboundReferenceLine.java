package com.ruoyi.mes.purchase.domain.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 可参照生成入库单的送货单合格明细。 */
@Data
public class InboundReferenceLine {
    private Long sourceReceiptId;
    private String sourceReceiptCode;
    private Long sourceReceiptLineId;
    private Integer sourceReceiptLineNo;
    private Long sourceOrderId;
    private String sourceOrderCode;
    private Long sourceOrderLineId;
    private Integer sourceOrderLineNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
    private String lotNo;
    private String productionDate;
    private String expiryDate;
    private BigDecimal qualifiedQuantity;
    private BigDecimal inboundQuantity;
    private BigDecimal remainingQuantity;
}
