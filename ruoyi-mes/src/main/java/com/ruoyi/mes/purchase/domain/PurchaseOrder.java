package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购订单主表。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity {

    private Long id;

    @NotBlank(message = "订单编号不能为空")
    private String orderCode;

    private String supplierCode;

    @NotBlank(message = "供应商不能为空")
    private String supplierName;

    @NotBlank(message = "订单日期不能为空")
    private String orderDate;
    private String expectedDate;
    @NotBlank(message = "订单状态不能为空")
    private String status;
    private String approvedBy;
    private Date approvedTime;
    @NotNull(message = "订单总数量不能为空")
    @DecimalMin(value = "0.0", message = "订单总数量不能小于0")
    private BigDecimal totalQuantity;
    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.0", message = "订单总金额不能小于0")
    private BigDecimal totalAmount;
    @NotBlank(message = "币种不能为空")
    private String currency;
    @Valid
    @NotEmpty(message = "采购订单至少需要一条明细")
    private List<PurchaseOrderLine> lines;
}
