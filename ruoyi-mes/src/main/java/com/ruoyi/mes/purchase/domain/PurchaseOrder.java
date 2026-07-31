package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    /** 服务端按采购行的 inputQty 汇总；多单位订单不把它作为换算基准。 */
    @DecimalMin(value = "0.0", message = "订单总数量不能小于0")
    private BigDecimal totalQuantity;
    /** 服务端按采购行的 inputQty * unitPrice 汇总。 */
    @DecimalMin(value = "0.0", message = "订单总金额不能小于0")
    private BigDecimal totalAmount;
    @NotBlank(message = "币种不能为空")
    private String currency;

    /** 单据类型：NORMAL */
    private String billType;
    @Valid
    @NotEmpty(message = "采购订单至少需要一条明细")
    private List<PurchaseOrderLine> lines;
}
