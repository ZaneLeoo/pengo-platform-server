package com.ruoyi.mes.base.domain.dto;

/** 将 BOM OCR 导入草稿应用到正式 BOM 版本的请求。 */
public class BomImportApplyRequest {
    /** 目标 BOM 版本 ID。 */
    private Long bomVersionId;

    public Long getBomVersionId() {
        return bomVersionId;
    }

    public void setBomVersionId(Long bomVersionId) {
        this.bomVersionId = bomVersionId;
    }
}
