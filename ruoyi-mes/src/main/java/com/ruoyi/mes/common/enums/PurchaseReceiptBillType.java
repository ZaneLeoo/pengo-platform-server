package com.ruoyi.mes.common.enums;

/** 到货单单据类型。 */
public enum PurchaseReceiptBillType {
    DIRECT("DIRECT", "无来源"), PURCHASE_ORDER("PURCHASE_ORDER", "采购订单");

    private final String code;
    private final String label;

    PurchaseReceiptBillType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
