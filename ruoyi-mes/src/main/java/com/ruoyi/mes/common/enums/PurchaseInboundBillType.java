package com.ruoyi.mes.common.enums;

/** 入库单单据类型。 */
public enum PurchaseInboundBillType {
    DIRECT("DIRECT", "无来源"),
    RECEIPT("RECEIPT", "到货单");

    private final String code;
    private final String label;

    PurchaseInboundBillType(String code, String label)
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
