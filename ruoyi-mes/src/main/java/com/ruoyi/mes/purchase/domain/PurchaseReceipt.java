package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购到货单主表。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseReceipt extends BaseEntity {
    private Long id;
    @NotBlank(message = "到货单编号不能为空") private String receiptCode;
    @NotNull(message = "采购订单不能为空") private Long orderId;
    @NotBlank(message = "采购订单编号不能为空") private String orderCode;
    private String supplierCode;
    private String supplierName;
    @NotBlank(message = "到货日期不能为空") private String receiptDate;
    @NotBlank(message = "到货单状态不能为空") private String status;
    @NotBlank(message = "检验状态不能为空") private String inspectionStatus;
    @NotNull(message = "到货总数量不能为空") @DecimalMin(value = "0.0", message = "到货总数量不能小于0") private BigDecimal totalQuantity;
    @Valid @NotEmpty(message = "到货单至少需要一条明细") private List<PurchaseReceiptLine> lines;
}
