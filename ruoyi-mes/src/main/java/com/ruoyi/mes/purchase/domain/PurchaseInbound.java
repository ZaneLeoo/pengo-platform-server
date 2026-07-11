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

/** 采购入库单主表。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseInbound extends BaseEntity {
    private Long id;
    @NotBlank(message = "入库单编号不能为空")
    private String inboundCode;
    @NotNull(message = "到货单不能为空")
    private Long receiptId;
    @NotBlank(message = "到货单编号不能为空")
    private String receiptCode;
    @NotBlank(message = "入库日期不能为空")
    private String inboundDate;
    @NotBlank(message = "入库仓库不能为空")
    private String warehouseCode;
    private String warehouseName;
    @NotBlank(message = "入库单状态不能为空")
    private String status;
    @NotNull(message = "入库总数量不能为空")
    @DecimalMin(value = "0.0", message = "入库总数量不能小于0")
    private BigDecimal totalQuantity;
    @Valid
    @NotEmpty(message = "入库单至少需要一条明细")
    private List<PurchaseInboundLine> lines;
}
