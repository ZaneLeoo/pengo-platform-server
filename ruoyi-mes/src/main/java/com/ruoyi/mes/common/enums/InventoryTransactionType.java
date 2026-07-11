package com.ruoyi.mes.common.enums;

/** 库存流水业务类型。 */
public enum InventoryTransactionType {
    INBOUND("INBOUND", "采购入库"),
    INBOUND_REVERSE("INBOUND_REVERSE", "采购入库弃审冲回");

    private final String code;
    private final String label;

    InventoryTransactionType(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
