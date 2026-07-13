package com.ruoyi.mes.common.enums;

/**
 * BOM子件供应/扣料方式。
 *
 * @author ruoyi
 */
public enum SupplyType {
    PUSH("PUSH", "推式领料"), STOCK_BACKFLUSH("STOCK_BACKFLUSH", "入库倒冲"), PROCESS_BACKFLUSH("PROCESS_BACKFLUSH",
            "工序倒冲"), VIRTUAL("VIRTUAL", "虚拟件"), DIRECT("DIRECT", "直接供应");

    private final String code;
    private final String label;

    SupplyType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 获取方式编码。
     *
     * @return 方式编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取方式名称。
     *
     * @return 方式名称
     */
    public String getLabel() {
        return label;
    }
}
