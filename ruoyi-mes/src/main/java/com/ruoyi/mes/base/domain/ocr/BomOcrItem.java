package com.ruoyi.mes.base.domain.ocr;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

/** BOM 图纸明细行识别结果。 */
public class BomOcrItem
{
    /** 明细行序号。 */
    @JSONField(alternateNames = {"line_no", "序号"})
    private Integer lineNo;

    /** 子件编码候选，优先来自配件编号、物料编码、代号等。 */
    @JsonAlias({"componentCode", "materialCode", "partCode"})
    @JSONField(alternateNames = {"componentCode", "materialCode", "partCode", "material_code", "component_code", "code", "图号/物料编码", "物料编码", "配件编号"})
    private String componentCodeCandidate;

    /** 子件图号。 */
    @JSONField(alternateNames = {"drawing_no", "drawingCode", "图号", "产品图号"})
    private String drawingNo;

    /** 子件名称。 */
    @JSONField(alternateNames = {"name", "componentName", "materialName", "item_name", "名称"})
    private String itemName;

    /** 子件数量，对应 BomItem.componentQty 候选。 */
    @JSONField(alternateNames = {"qty", "componentQty", "amount", "数量"})
    private BigDecimal quantity;

    /** 规格型号/材料/标准号。 */
    @JSONField(alternateNames = {"model", "specification", "规格", "规格/型号"})
    private String spec;

    /** 单位；图纸没有时为空，后端从物料主数据或默认值补齐。 */
    @JSONField(alternateNames = {"componentUnit", "itemUnit", "单位"})
    private String unit;

    /** 子件类型/属性，例如成品、半成品、自制件、外购件。 */
    @JsonAlias({"itemType", "type", "componentType", "componentAttribute", "itemAttribute"})
    @JSONField(alternateNames = {"type", "item_type", "componentType", "componentAttribute", "itemAttribute", "component_attribute", "类型", "属性"})
    private String itemType;

    /** 单件重量，仅作参考。 */
    @JSONField(alternateNames = {"singleWeight", "weight", "单件重量", "重量单件", "重量（kg）单件"})
    private BigDecimal unitWeight;

    /** 总重量，仅作参考。 */
    @JSONField(alternateNames = {"sumWeight", "total_weight", "总重量", "重量总计", "重量（kg）总计"})
    private BigDecimal totalWeight;

    /** 备注。 */
    @JSONField(alternateNames = {"note", "remarks", "备注"})
    private String remark;

    /** 原始行文本，便于人工追溯。 */
    private String rawText;

    /** 模型对本行识别结果的自评置信度，0 到 1，仅作参考。 */
    private BigDecimal confidence;

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

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

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
}
