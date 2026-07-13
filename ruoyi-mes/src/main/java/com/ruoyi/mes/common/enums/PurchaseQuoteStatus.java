package com.ruoyi.mes.common.enums;

/** 供应商报价单状态。 */
public enum PurchaseQuoteStatus {
    DRAFT("DRAFT", "草稿"),
    APPROVED("APPROVED", "已审核"),
    EXPIRED("EXPIRED", "已过期"),
    CANCELLED("CANCELLED", "已作废");

    private final String code;
    private final String label;

    PurchaseQuoteStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PurchaseQuoteStatus fromCode(String code) {
        for (PurchaseQuoteStatus value : values()) {
            if (value.code.equals(code)) return value;
        }
        return null;
    }
}
