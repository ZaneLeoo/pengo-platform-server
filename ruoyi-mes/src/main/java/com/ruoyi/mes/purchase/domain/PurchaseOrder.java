package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 采购订单主表。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity
{
    private Long id;
    private String orderCode;
    private String supplierCode;
    private String supplierName;
    private String orderDate;
    private String expectedDate;
    private String status;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private String currency;
    private List<PurchaseOrderLine> lines;
}
