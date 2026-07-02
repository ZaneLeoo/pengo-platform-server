package com.ruoyi.mes.base.domain.dto;

import java.math.BigDecimal;

/** BOM OCR 导入草稿明细。 */
public class BomImportDraftItem
{
    /** 草稿明细 ID。 */
    private Long id;

    /** 明细行序号。 */
    private Integer lineNo;

    /** 子件编码候选。 */
    private String componentCodeCandidate;

    /** 子件图号。 */
    private String drawingNo;

    /** 子件名称。 */
    private String itemName;

    /** 子件数量候选。 */
    private BigDecimal quantity;

    /** 规格型号/材料/标准号。 */
    private String spec;

    /** 单位。 */
    private String unit;

    /** 单件重量，仅作参考。 */
    private BigDecimal unitWeight;

    /** 总重量，仅作参考。 */
    private BigDecimal totalWeight;

    /** 备注。 */
    private String remark;

    /** 原始行文本，便于人工追溯。 */
    private String rawText;

    /** 模型置信度，仅供参考。 */
    private BigDecimal confidence;

    /** 匹配到的系统物料 ID。 */
    private Long matchedMaterialId;

    /** 匹配到的系统物料编码。 */
    private String matchedMaterialCode;

    /** 匹配到的系统物料名称。 */
    private String matchedMaterialName;

    /** 物料匹配状态。 */
    private String matchStatus;

    /** 行风险等级。 */
    private String riskLevel;

    /** 行级问题描述。 */
    private String issueMessage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }

    public String getComponentCodeCandidate() { return componentCodeCandidate; }
    public void setComponentCodeCandidate(String componentCodeCandidate) { this.componentCodeCandidate = componentCodeCandidate; }

    public String getDrawingNo() { return drawingNo; }
    public void setDrawingNo(String drawingNo) { this.drawingNo = drawingNo; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitWeight() { return unitWeight; }
    public void setUnitWeight(BigDecimal unitWeight) { this.unitWeight = unitWeight; }

    public BigDecimal getTotalWeight() { return totalWeight; }
    public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public Long getMatchedMaterialId() { return matchedMaterialId; }
    public void setMatchedMaterialId(Long matchedMaterialId) { this.matchedMaterialId = matchedMaterialId; }

    public String getMatchedMaterialCode() { return matchedMaterialCode; }
    public void setMatchedMaterialCode(String matchedMaterialCode) { this.matchedMaterialCode = matchedMaterialCode; }

    public String getMatchedMaterialName() { return matchedMaterialName; }
    public void setMatchedMaterialName(String matchedMaterialName) { this.matchedMaterialName = matchedMaterialName; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getIssueMessage() { return issueMessage; }
    public void setIssueMessage(String issueMessage) { this.issueMessage = issueMessage; }
}
