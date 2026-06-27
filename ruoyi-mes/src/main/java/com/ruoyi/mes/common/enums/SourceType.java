package com.ruoyi.mes.common.enums;

/**
 * MES物料来源类型。
 *
 * @author ruoyi
 */
public enum SourceType {

    /** 自制 */
    MAKE("MAKE", "自制"),

    /** 外购 */
    PURCHASE("PURCHASE", "外购"),

    /** 委外 */
    OUTSOURCE("OUTSOURCE", "委外");

    private final String code;

    private final String label;

    SourceType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取来源编码。
     *
     * @return 来源编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取来源名称。
     *
     * @return 来源名称
     */
    public String getLabel() {
        return label;
    }
}
