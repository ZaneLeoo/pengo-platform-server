package com.ruoyi.web.domain.enums;

/** BOM AI 图纸导入追溯记录状态。 */
public enum BomAiImportTraceStatus {
    RECOGNIZING("识别中"),
    RECOGNIZED("待确认"),
    IMPORTING("导入中"),
    FAILED("识别失败"),
    IMPORTED("已导入"),
    CANCELLED("已放弃"),
    EXPIRED("已过期");

    private final String label;

    BomAiImportTraceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "未知";
        }
        for (BomAiImportTraceStatus status : values()) {
            if (status.name().equals(code)) {
                return status.label;
            }
        }
        return code;
    }
}
