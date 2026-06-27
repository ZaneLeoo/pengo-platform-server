package com.ruoyi.mes.common.enums;

/**
 * MES物料类型。
 *
 * @author ruoyi
 */
public enum MaterialType {

    /** 原材料 */
    RAW("RAW", "原材料"),

    /** 半成品 */
    SEMI_FINISHED("SEMI_FINISHED", "半成品"),

    /** 成品 */
    FINISHED("FINISHED", "成品"),

    /** 辅料 */
    AUXILIARY("AUXILIARY", "辅料"),

    /** 包装物 */
    PACKAGE("PACKAGE", "包装物");

    private final String code;

    private final String label;

    MaterialType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取物料类型编码。
     *
     * @return 物料类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取物料类型名称。
     *
     * @return 物料类型名称
     */
    public String getLabel() {
        return label;
    }
}
