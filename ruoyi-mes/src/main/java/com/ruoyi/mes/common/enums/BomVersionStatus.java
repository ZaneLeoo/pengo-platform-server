package com.ruoyi.mes.common.enums;

/**
 * BOM版本状态。
 *
 * @author ruoyi
 */
public enum BomVersionStatus {
    DRAFT("DRAFT", "草稿"), EFFECTIVE("EFFECTIVE", "生效"), FROZEN("FROZEN", "冻结");

    private final String code;
    private final String label;

    BomVersionStatus(String code, String label) {
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
