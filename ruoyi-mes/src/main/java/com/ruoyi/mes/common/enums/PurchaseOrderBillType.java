package com.ruoyi.mes.common.enums;

/** 采购订单单据类型。 */
public enum PurchaseOrderBillType {
    NORMAL("NORMAL", "正常采购");

    private final String code;
    private final String label;

    PurchaseOrderBillType(String code, String label) {
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
