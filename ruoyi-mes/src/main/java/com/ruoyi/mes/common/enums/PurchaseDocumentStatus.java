package com.ruoyi.mes.common.enums;

/** 采购单据审核状态。 */
public enum PurchaseDocumentStatus {
    DRAFT("DRAFT", "草稿"),
    APPROVED("APPROVED", "已审核");

    private final String code;
    private final String label;

    PurchaseDocumentStatus(String code, String label)
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
