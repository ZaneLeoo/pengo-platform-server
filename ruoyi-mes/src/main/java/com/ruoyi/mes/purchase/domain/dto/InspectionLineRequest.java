package com.ruoyi.mes.purchase.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/** 送货单单行质检结果。 */
@Data
public class InspectionLineRequest {
    @NotNull(message = "送货明细不能为空")
    private Long receiptLineId;
    @NotNull(message = "合格数量不能为空")
    @DecimalMin(value = "0.0", message = "合格数量不能小于0")
    private BigDecimal qualifiedQuantity;
    @NotNull(message = "不合格数量不能为空")
    @DecimalMin(value = "0.0", message = "不合格数量不能小于0")
    private BigDecimal rejectedQuantity;
}
