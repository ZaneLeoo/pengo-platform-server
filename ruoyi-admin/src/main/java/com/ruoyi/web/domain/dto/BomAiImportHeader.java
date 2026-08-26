package com.ruoyi.web.domain.dto;

import java.math.BigDecimal;

/** BOM AI 导入预览 — 母件/文档头信息。 */
public class BomAiImportHeader {

    /** AI 识别的母件编码 */
    private String parentItemCode;

    /** AI 识别的母件名称 */
    private String parentItemName;

    /** AI 识别的母件规格 */
    private String parentItemSpec;

    /** 图号 */
    private String drawingNo;

    /** 版次 */
    private String revision;

    /** 基准数量 */
    private BigDecimal baseQty;

    /** 是否匹配到系统物料 */
    private boolean matched;

    /** 匹配到的物料 ID */
    private Long matchedMaterialId;

    /** 匹配到的物料编码 */
    private String matchedMaterialCode;

    /** 匹配到的物料名称 */
    private String matchedMaterialName;

    // ── 用户编辑确认字段 ──

    private String finalParentItemCode;
    private String finalParentItemName;
    private String finalParentItemSpec;
    private BigDecimal finalBaseQty;

    /** 用户选择/更改的匹配物料 ID */
    private Long finalParentMaterialId;

    public String getParentItemCode() {
        return parentItemCode;
    }

    public void setParentItemCode(String parentItemCode) {
        this.parentItemCode = parentItemCode;
    }

    public String getParentItemName() {
        return parentItemName;
    }

    public void setParentItemName(String parentItemName) {
        this.parentItemName = parentItemName;
    }

    public String getParentItemSpec() {
        return parentItemSpec;
    }

    public void setParentItemSpec(String parentItemSpec) {
        this.parentItemSpec = parentItemSpec;
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

    public BigDecimal getBaseQty() {
        return baseQty;
    }

    public void setBaseQty(BigDecimal baseQty) {
        this.baseQty = baseQty;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public Long getMatchedMaterialId() {
        return matchedMaterialId;
    }

    public void setMatchedMaterialId(Long matchedMaterialId) {
        this.matchedMaterialId = matchedMaterialId;
    }

    public String getMatchedMaterialCode() {
        return matchedMaterialCode;
    }

    public void setMatchedMaterialCode(String matchedMaterialCode) {
        this.matchedMaterialCode = matchedMaterialCode;
    }

    public String getMatchedMaterialName() {
        return matchedMaterialName;
    }

    public void setMatchedMaterialName(String matchedMaterialName) {
        this.matchedMaterialName = matchedMaterialName;
    }

    public String getFinalParentItemCode() {
        return finalParentItemCode;
    }

    public void setFinalParentItemCode(String finalParentItemCode) {
        this.finalParentItemCode = finalParentItemCode;
    }

    public String getFinalParentItemName() {
        return finalParentItemName;
    }

    public void setFinalParentItemName(String finalParentItemName) {
        this.finalParentItemName = finalParentItemName;
    }

    public String getFinalParentItemSpec() {
        return finalParentItemSpec;
    }

    public void setFinalParentItemSpec(String finalParentItemSpec) {
        this.finalParentItemSpec = finalParentItemSpec;
    }

    public BigDecimal getFinalBaseQty() {
        return finalBaseQty;
    }

    public void setFinalBaseQty(BigDecimal finalBaseQty) {
        this.finalBaseQty = finalBaseQty;
    }

    public Long getFinalParentMaterialId() {
        return finalParentMaterialId;
    }

    public void setFinalParentMaterialId(Long finalParentMaterialId) {
        this.finalParentMaterialId = finalParentMaterialId;
    }
}
