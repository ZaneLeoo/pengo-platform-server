package com.ruoyi.mes.common.enums;

/**
 * BOM版本用途。
 *
 * @author ruoyi
 */
public enum BomUsageType {
    GENERAL("GENERAL", "通用"), MASS("MASS", "量产"), TRIAL("TRIAL", "试制"), REWORK("REWORK", "返工");

    private final String code;
    private final String label;

    BomUsageType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取用途编码。
     *
     * @return 用途编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取用途名称。
     *
     * @return 用途名称
     */
    public String getLabel() {
        return label;
    }
}
