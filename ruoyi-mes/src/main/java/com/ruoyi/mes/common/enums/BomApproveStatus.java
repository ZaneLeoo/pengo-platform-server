package com.ruoyi.mes.common.enums;

/**
 * BOM审批状态。
 *
 * @author ruoyi
 */
public enum BomApproveStatus {
    UNAPPROVED("UNAPPROVED", "未审核"),
    PENDING("PENDING", "审核中"),
    APPROVED("APPROVED", "已审核"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String label;

    BomApproveStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取状态编码。
     *
     * @return 状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态名称。
     *
     * @return 状态名称
     */
    public String getLabel() {
        return label;
    }
}
