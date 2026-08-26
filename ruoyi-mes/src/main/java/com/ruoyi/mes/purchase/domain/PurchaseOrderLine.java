package com.ruoyi.mes.purchase.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ruoyi.common.core.domain.BaseEntity;
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

    private String materialCode;
    private String materialName;
    private String spec;
    private String model;

    /** 兼容字段：保存时等于 inputUnitCode，不再作为换算中心。 */
    private String unit;

    /** 物料绑定的计量单位组编码，查询和前端回显使用，不落采购明细表。 */
    private String unitGroupCode;

    /** 录入单位编码（用户实际使用的输入单位） */
    private String inputUnitCode;

    /** 录入单位名称 */
    private String inputUnitName;

    /** 用户实际录入的数量，按 inputUnitCode 计价。 */
    @JsonAlias("inputQuantity")
    private BigDecimal inputQty;

    /** 单位组成员1快照。顺序不表示主次。 */
    private String unit1Code;

    private String unit1Name;
    private BigDecimal unit1Qty;

    /** 单位组成员2快照。顺序不表示主次。 */
    private String unit2Code;

    private String unit2Name;
    private BigDecimal unit2Qty;

    /** 单位组成员3快照。顺序不表示主次。 */
    private String unit3Code;

    private String unit3Name;
    private BigDecimal unit3Qty;

    /** 兼容字段：保存时等于 inputQty，不再表示库存基准数量。 */
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
