package com.ruoyi.web.domain.dto;

/**
 * BOM AI 批量导入成功后生成的 BOM 摘要。
 */
public class BomAiImportedBom {

    private Long bomMasterId;
    private Long bomVersionId;
    private String bomCode;
    private String parentItemCode;
    private String parentItemName;

    public Long getBomMasterId() { return bomMasterId; }
    public void setBomMasterId(Long bomMasterId) { this.bomMasterId = bomMasterId; }
    public Long getBomVersionId() { return bomVersionId; }
    public void setBomVersionId(Long bomVersionId) { this.bomVersionId = bomVersionId; }
    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }
    public String getParentItemCode() { return parentItemCode; }
    public void setParentItemCode(String parentItemCode) { this.parentItemCode = parentItemCode; }
    public String getParentItemName() { return parentItemName; }
    public void setParentItemName(String parentItemName) { this.parentItemName = parentItemName; }
}
