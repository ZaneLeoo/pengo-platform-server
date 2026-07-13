package com.ruoyi.mes.common.enums;

/**
 * BOM类型。
 *
 * @author ruoyi
 */
public enum BomType {
    MANUFACTURING("MANUFACTURING", "生产BOM"), TRIAL("TRIAL", "试制BOM"), REWORK("REWORK", "返工BOM");

    private final String code;
    private final String label;

    BomType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取类型编码。
     *
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型名称。
     *
     * @return 类型名称
     */
    public String getLabel() {
        return label;
    }
}
