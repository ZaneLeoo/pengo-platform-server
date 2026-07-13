package com.ruoyi.mes.common.enums;

/** 供应商报价来源。 */
public enum PurchaseQuoteSourceType {
    MANUAL("MANUAL", "手工录入"),
    EXCEL("EXCEL", "Excel导入"),
    API("API", "接口同步");

    private final String code;
    private final String label;

    PurchaseQuoteSourceType(String code, String label) {
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
