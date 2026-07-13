package com.ruoyi.mes.common.enums;

/**
 * 数据来源系统。
 *
 * @author ruoyi
 */
public enum SourceSystem {
    MANUAL("MANUAL", "手工"), EXCEL("EXCEL", "Excel"), U8("U8", "U8"), U9C("U9C", "U9C"), AI_IMPORT("AI_IMPORT", "AI导入");

    private final String code;
    private final String label;

    SourceSystem(String code, String label) {
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
