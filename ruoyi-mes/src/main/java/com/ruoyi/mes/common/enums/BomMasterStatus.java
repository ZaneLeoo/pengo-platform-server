package com.ruoyi.mes.common.enums;

/**
 * BOM主表状态。
 *
 * @author ruoyi
 */
public enum BomMasterStatus {
    ENABLED("ENABLED", "启用"), DISABLED("DISABLED", "停用");

    private final String code;
    private final String label;

    BomMasterStatus(String code, String label) {
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
