package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 供应商报价明细，支持按采购数量匹配阶梯价格。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseSupplierQuoteLine extends BaseEntity {

    private Long id;

    private Long quoteId;

    @NotNull(message = "报价明细行号不能为空")
    private Integer lineNo;

    @NotNull(message = "报价物料不能为空")
    private Long materialId;

    @NotBlank(message = "报价物料编码不能为空")
    private String materialCode;

    @NotBlank(message = "报价物料名称不能为空")
    private String materialName;

    private String spec;

    @NotBlank(message = "报价物料单位不能为空")
    private String unit;

    @NotNull(message = "最小起订量不能为空")
    @DecimalMin(value = "0.000001", message = "最小起订量必须大于0")
    private BigDecimal minOrderQuantity;

    /** 阶梯价格起始数量，空时按 0 处理。 */
    @DecimalMin(value = "0.000000", message = "阶梯起始数量不能小于0")
    private BigDecimal minQuantity;

    /** 阶梯价格结束数量，空表示没有上限。 */
    @DecimalMin(value = "0.000000", message = "阶梯结束数量不能小于0")
    private BigDecimal maxQuantity;

    @NotNull(message = "报价单价不能为空")
    @DecimalMin(value = "0.000001", message = "报价单价必须大于0")
    private BigDecimal unitPrice;

    @NotNull(message = "税率不能为空")
    @DecimalMin(value = "0.0000", message = "税率不能小于0")
    private BigDecimal taxRate;

    @NotNull(message = "交货周期不能为空")
    @DecimalMin(value = "0", message = "交货周期不能小于0")
    private Integer leadTimeDays;
}
