package com.ruoyi.mes.common.enums;

/** 采购到货质检状态。 */
public enum ReceiptInspectionStatus {
    PENDING("PENDING", "待检"),
    PASSED("PASSED", "合格"),
    PARTIAL("PARTIAL", "部分合格"),
    FAILED("FAILED", "不合格");

    private final String code;
    private final String label;

    ReceiptInspectionStatus(String code, String label)
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
