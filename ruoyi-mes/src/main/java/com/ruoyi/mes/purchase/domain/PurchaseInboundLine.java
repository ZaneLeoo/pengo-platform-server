package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购入库单明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseInboundLine extends BaseEntity {
    private Long id;
    private Long inboundId;
    @NotNull(message = "入库明细行号不能为空")
    private Integer lineNo;
    @NotNull(message = "来源到货单不能为空")
    private Long sourceReceiptId;
    @NotBlank(message = "来源到货单编号不能为空")
    private String sourceReceiptCode;
    @NotNull(message = "来源到货单明细不能为空")
    private Long sourceReceiptLineId;
    @NotNull(message = "来源到货单明细行号不能为空")
    private Integer sourceReceiptLineNo;
    @NotNull(message = "来源采购订单不能为空")
    private Long sourceOrderId;
    @NotBlank(message = "来源采购订单编号不能为空")
    private String sourceOrderCode;
    @NotNull(message = "来源采购订单明细不能为空")
    private Long sourceOrderLineId;
    @NotNull(message = "来源采购订单明细行号不能为空")
    private Integer sourceOrderLineNo;
    @NotNull(message = "入库物料不能为空")
    private Long materialId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String spec;
    @NotBlank(message = "物料单位不能为空")
    private String unit;
    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0.000001", message = "入库数量必须大于0")
    private BigDecimal inboundQuantity;
    private String lotNo;
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
}
