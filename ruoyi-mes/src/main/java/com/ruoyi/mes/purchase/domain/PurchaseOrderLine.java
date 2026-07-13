package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购订单明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderLine extends BaseEntity {
    private Long id;
    private Long orderId;
    @NotNull(message = "采购明细行号不能为空")
    private Integer lineNo;
    @NotNull(message = "采购物料不能为空")
    private Long materialId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String spec;
    private String model;
    @NotBlank(message = "物料单位不能为空")
    private String unit;
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.000001", message = "采购数量必须大于0")
    private BigDecimal orderQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal qualifiedQuantity;
    private BigDecimal inboundQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private String plannedDate;
    /** 使用的供应商报价主表ID，仅用于追溯，不在普通录入界面展示。 */
    private Long quoteId;
    /** 使用的供应商报价明细ID，仅用于追溯，不在普通录入界面展示。 */
    private Long quoteLineId;
    /** 价格来源：MANUAL 或 QUOTE。 */
    private String priceSource;
}
