package com.ruoyi.mes.base.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

/** BOM OCR 导入任务。 */
public class BomImportTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 导入任务 ID。 */
    private Long id;

    /** 原始文件名称。 */
    private String fileName;

    /** 原始文件访问地址。 */
    private String fileUrl;

    /** 原始文件类型，例如 image、pdf。 */
    private String fileType;

    /** 导入任务状态：processing、draft、recognized、validated、imported、failed。 */
    private String status;

    /** 图纸标题。 */
    private String title;

    /** 母件名称候选。 */
    private String parentNameCandidate;

    /** 母件编码候选。 */
    private String parentCodeCandidate;

    /** 产品型号/规格型号。 */
    private String productModel;

    /** 图纸编号/部件图号/装配图号。 */
    private String drawingNo;

    /** 版本/版次，对应 BOM 版本候选。 */
    private String revision;

    /** 母件基准数量/底数候选。 */
    private BigDecimal baseQtyCandidate;

    /** 图纸声明的总明细行数。 */
    private Integer totalRows;

    /** 单件重量，仅作参考。 */
    private BigDecimal unitWeight;

    /** Dify 原始输出 JSON。 */
    private String rawResultJson;

    /** 标题区扩展字段 JSON。 */
    private String extraFieldsJson;

    /** 问题列表 JSON。 */
    private String issuesJson;

    /** 错误信息。 */
    private String errorMessage;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

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

    public String getRawResultJson() {
        return rawResultJson;
    }
    public void setRawResultJson(String rawResultJson) {
        this.rawResultJson = rawResultJson;
    }

    public String getExtraFieldsJson() {
        return extraFieldsJson;
    }
    public void setExtraFieldsJson(String extraFieldsJson) {
        this.extraFieldsJson = extraFieldsJson;
    }

    public String getIssuesJson() {
        return issuesJson;
    }
    public void setIssuesJson(String issuesJson) {
        this.issuesJson = issuesJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
