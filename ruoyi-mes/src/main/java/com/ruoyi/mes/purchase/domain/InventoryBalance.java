package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 库存余额。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryBalance extends BaseEntity {
    private Long id;
    @NotNull(message = "物料不能为空")
    private Long materialId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
    private String lotNo;
    @NotBlank(message = "计量单位不能为空")
    private String unit;
    @NotNull(message = "库存数量不能为空")
    @DecimalMin(value = "0.0", message = "库存数量不能小于0")
    private BigDecimal quantity;
    @NotNull(message = "可用数量不能为空")
    @DecimalMin(value = "0.0", message = "可用数量不能小于0")
    private BigDecimal availableQuantity;
    @NotNull(message = "锁定数量不能为空")
    @DecimalMin(value = "0.0", message = "锁定数量不能小于0")
    private BigDecimal lockedQuantity;
    @NotBlank(message = "库存状态不能为空")
    private String status;
    private String lastInboundDate;
    private String lastUpdateTime;
}
