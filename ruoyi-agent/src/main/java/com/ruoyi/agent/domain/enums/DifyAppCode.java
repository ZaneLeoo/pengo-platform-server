package com.ruoyi.agent.domain.enums;

/** Dify 应用编码。 */
public enum DifyAppCode
{
    /** 企业智能体聊天应用。 */
    AGENT_CHAT("AGENT_CHAT"),

    /** BOM 图纸 OCR 识别应用。 */
    BOM_OCR("BOM_OCR");

    private final String code;

    DifyAppCode(String code) { this.code = code; }

    /** 返回配置表中的应用编码。 */
    public String getCode() { return code; }
}
