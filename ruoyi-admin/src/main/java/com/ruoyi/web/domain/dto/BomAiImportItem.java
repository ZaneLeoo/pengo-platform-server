package com.ruoyi.web.domain.dto;

import java.math.BigDecimal;

/**
 * BOM AI 导入预览 — 子件明细行。
 */
public class BomAiImportItem {

    private Integer lineNo;
    private String componentCode;
    private String drawingNo;
    private String itemName;
    private BigDecimal quantity;
    private String spec;
    private String unit;
    private String remark;

    private boolean matched;
    private Long matchedMaterialId;
    private String matchedMaterialCode;
    private String matchedMaterialName;
    private String matchedMaterialSpec;
    private String matchedMaterialUnit;

    private String finalItemCode;
    private String finalItemName;
    private BigDecimal finalQuantity;
    private String finalSpec;
    private String finalUnit;

    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public String getComponentCode() { return componentCode; }
    public void setComponentCode(String componentCode) { this.componentCode = componentCode; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }
    public Long getMatchedMaterialId() { return matchedMaterialId; }
    public void setMatchedMaterialId(Long matchedMaterialId) { this.matchedMaterialId = matchedMaterialId; }
    public String getMatchedMaterialCode() { return matchedMaterialCode; }
    public void setMatchedMaterialCode(String matchedMaterialCode) { this.matchedMaterialCode = matchedMaterialCode; }
    public String getMatchedMaterialName() { return matchedMaterialName; }
    public void setMatchedMaterialName(String matchedMaterialName) { this.matchedMaterialName = matchedMaterialName; }
    public String getMatchedMaterialSpec() { return matchedMaterialSpec; }
    public void setMatchedMaterialSpec(String matchedMaterialSpec) { this.matchedMaterialSpec = matchedMaterialSpec; }
    public String getMatchedMaterialUnit() { return matchedMaterialUnit; }
    public void setMatchedMaterialUnit(String matchedMaterialUnit) { this.matchedMaterialUnit = matchedMaterialUnit; }
    public String getFinalItemCode() { return finalItemCode; }
    public void setFinalItemCode(String finalItemCode) { this.finalItemCode = finalItemCode; }
    public String getFinalItemName() { return finalItemName; }
    public void setFinalItemName(String finalItemName) { this.finalItemName = finalItemName; }
    public BigDecimal getFinalQuantity() { return finalQuantity; }
    public void setFinalQuantity(BigDecimal finalQuantity) { this.finalQuantity = finalQuantity; }
    public String getFinalSpec() { return finalSpec; }
    public void setFinalSpec(String finalSpec) { this.finalSpec = finalSpec; }
    public String getFinalUnit() { return finalUnit; }
    public void setFinalUnit(String finalUnit) { this.finalUnit = finalUnit; }
}
