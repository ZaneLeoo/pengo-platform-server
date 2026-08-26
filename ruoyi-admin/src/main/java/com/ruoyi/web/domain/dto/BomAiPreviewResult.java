package com.ruoyi.web.domain.dto;

import java.util.List;

/** BOM AI 导入 — 预览结果。 */
public class BomAiPreviewResult {

    /** 母件信息 */
    private BomAiImportHeader header;

    /** 子件明细 */
    private List<BomAiImportItem> items;

    /** 多图片识别出的多个独立 BOM。 */
    private List<BomAiDocument> documents;

    /** 本次识别的可追溯记录 ID。 */
    private Long traceId;

    /** 本次识别的可追溯批次号。 */
    private String importNo;

    private Long duplicateImportedTraceId;
    private String duplicateImportedImportNo;

    /** 是否识别成功 */
    private boolean success;

    /** 错误信息 */
    private String error;

    public BomAiImportHeader getHeader() {
        return header;
    }

    public void setHeader(BomAiImportHeader header) {
        this.header = header;
    }

    public List<BomAiImportItem> getItems() {
        return items;
    }

    public void setItems(List<BomAiImportItem> items) {
        this.items = items;
    }

    public List<BomAiDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<BomAiDocument> documents) {
        this.documents = documents;
    }

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public String getImportNo() {
        return importNo;
    }

    public void setImportNo(String importNo) {
        this.importNo = importNo;
    }

    public Long getDuplicateImportedTraceId() {
        return duplicateImportedTraceId;
    }

    public void setDuplicateImportedTraceId(Long duplicateImportedTraceId) {
        this.duplicateImportedTraceId = duplicateImportedTraceId;
    }

    public String getDuplicateImportedImportNo() {
        return duplicateImportedImportNo;
    }

    public void setDuplicateImportedImportNo(String duplicateImportedImportNo) {
        this.duplicateImportedImportNo = duplicateImportedImportNo;
    }

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
}
