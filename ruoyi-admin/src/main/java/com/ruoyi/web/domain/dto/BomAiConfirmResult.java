package com.ruoyi.web.domain.dto;

import java.util.List;

/** BOM AI 导入 — 确认导入结果。 */
public class BomAiConfirmResult {

    private boolean success;
    private String error;
    private Long bomMasterId;
    private Long bomVersionId;
    private String bomCode;
    private List<BomAiImportedBom> boms;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getBomMasterId() {
        return bomMasterId;
    }

    public void setBomMasterId(Long bomMasterId) {
        this.bomMasterId = bomMasterId;
    }

    public Long getBomVersionId() {
        return bomVersionId;
    }

    public void setBomVersionId(Long bomVersionId) {
        this.bomVersionId = bomVersionId;
    }

    public String getBomCode() {
        return bomCode;
    }

    public void setBomCode(String bomCode) {
        this.bomCode = bomCode;
    }

    public List<BomAiImportedBom> getBoms() {
        return boms;
    }

    public void setBoms(List<BomAiImportedBom> boms) {
        this.boms = boms;
    }
}
