package com.ruoyi.agent.domain.enums;

/** Dify 应用编码。 */
public enum DifyAppCode {
    /** 企业智能助手 Dify 应用。 */
    AGENT_SUPERVISOR("AGENT_SUPERVISOR"),

    /** BOM 图纸 OCR 识别应用。 */
    BOM_OCR("BOM_OCR");

    private final String code;

    DifyAppCode(String code) {
        this.code = code;
    }

    /** 返回配置表中的应用编码。 */
    public String getCode() {
        return code;
    }
}
