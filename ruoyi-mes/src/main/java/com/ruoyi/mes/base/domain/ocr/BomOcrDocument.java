package com.ruoyi.mes.base.domain.ocr;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** BOM 图纸标题区识别结果。 */
public class BomOcrDocument {
    /** 图纸标题。 */
    @JSONField(alternateNames = {"documentTitle", "bomTitle", "标题"})
    private String title;

    /** 母件名称候选，来自产品名称、部件名称、装配名称等。 */
    @JsonAlias({"productName", "parentName"})
    @JSONField(alternateNames = {"productName", "parentName", "assemblyName", "产品名称", "母件名称"})
    private String parentNameCandidate;

    /** 母件编码候选，可能来自产品编号、产品名称、图号等。 */
    @JsonAlias({"productCode", "parentCode"})
    @JSONField(alternateNames = {"productCode", "parentCode", "materialCode", "产品编码", "产品图号", "母件编码"})
    private String parentCodeCandidate;

    /** 产品型号/规格型号。 */
    @JSONField(alternateNames = {"model", "product_model", "产品型号"})
    private String productModel;

    /** 图纸编号/部件图号/装配图号。 */
    @JSONField(alternateNames = {"drawing_no", "drawingCode", "productDrawingNo", "产品图号", "部件图号"})
    private String drawingNo;

    /** 版本/版次，对应 BOM 版本候选。 */
    @JSONField(alternateNames = {"version", "rev", "版本", "版次"})
    private String revision;

    /** 母件基准数量/底数候选，对应 BomVersion.baseQty。 */
    @JsonAlias({"totalQuantity", "baseQty"})
    @JSONField(alternateNames = {"totalQuantity", "baseQty", "baseQuantity", "母件底数", "总数量"})
    private BigDecimal baseQtyCandidate;

    /** 图纸声明的总明细行数，用于校验是否识别完整。 */
    @JSONField(alternateNames = {"rowCount", "itemCount", "总行数"})
    private Integer totalRows;

    /** 单件重量，仅作参考。 */
    @JSONField(alternateNames = {"singleWeight", "weight", "单件重量"})
    private BigDecimal unitWeight;

    /** 其他标题区字段，例如设计编号、出厂编号、日期、设计人等。 */
    private Map<String, Object> extraFields = new LinkedHashMap<>();

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getParentNameCandidate() {
        return parentNameCandidate;
    }
    public void setParentNameCandidate(String parentNameCandidate) {
        this.parentNameCandidate = parentNameCandidate;
    }

    public String getParentCodeCandidate() {
        return parentCodeCandidate;
    }
    public void setParentCodeCandidate(String parentCodeCandidate) {
        this.parentCodeCandidate = parentCodeCandidate;
    }

    public String getProductModel() {
        return productModel;
    }
    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public String getDrawingNo() {
        return drawingNo;
    }
    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }

    public BigDecimal getBaseQtyCandidate() {
        return baseQtyCandidate;
    }
    public void setBaseQtyCandidate(BigDecimal baseQtyCandidate) {
        this.baseQtyCandidate = baseQtyCandidate;
    }

    public Integer getTotalRows() {
        return totalRows;
    }
    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public BigDecimal getUnitWeight() {
        return unitWeight;
    }
    public void setUnitWeight(BigDecimal unitWeight) {
        this.unitWeight = unitWeight;
    }

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }
    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }
}
