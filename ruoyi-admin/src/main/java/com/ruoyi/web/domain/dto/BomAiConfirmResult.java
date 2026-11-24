package com.ruoyi.web.domain.dto;

import java.util.List;

/**
 * BOM AI 导入 — 确认导入结果。
 */
public class BomAiConfirmResult {

    private boolean success;
    private String error;
    private Long bomMasterId;
    private Long bomVersionId;
    private String bomCode;
    private List<BomAiImportedBom> boms;
    /** 本次确认导入时自动建档的物料数量。 */
    private int createdMaterialCount;
    /** 本次确认导入时自动建档的物料明细。 */
    private List<BomAiProvisionedMaterial> createdMaterials;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Long getBomMasterId() { return bomMasterId; }
    public void setBomMasterId(Long bomMasterId) { this.bomMasterId = bomMasterId; }
    public Long getBomVersionId() { return bomVersionId; }
    public void setBomVersionId(Long bomVersionId) { this.bomVersionId = bomVersionId; }
    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }
    public List<BomAiImportedBom> getBoms() { return boms; }
    public void setBoms(List<BomAiImportedBom> boms) { this.boms = boms; }
    public int getCreatedMaterialCount() { return createdMaterialCount; }
    public void setCreatedMaterialCount(int createdMaterialCount) { this.createdMaterialCount = createdMaterialCount; }
    public List<BomAiProvisionedMaterial> getCreatedMaterials() { return createdMaterials; }
    public void setCreatedMaterials(List<BomAiProvisionedMaterial> createdMaterials) {
        this.createdMaterials = createdMaterials;
    }
}
