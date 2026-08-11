package com.ruoyi.web.domain.dto;

import java.util.List;

/**
 * BOM AI 导入 — 确认导入请求。
 */
public class BomAiImportConfirmRequest {
    /** 识别阶段创建的可追溯记录 ID。 */
    private Long traceId;
    /** 相同原始文件已导入时，是否明确作为新版本再次导入。 */
    private boolean forceNewVersion;
    /** 强制重复导入原因。 */
    private String reimportReason;

    /** 母件信息（用户编辑后） */
    private BomAiImportHeader header;
    /** 子件明细（用户编辑后） */
    private List<BomAiImportItem> items;
    /** 多个独立 BOM（用户编辑后）。 */
    private List<BomAiDocument> documents;

    public Long getTraceId() { return traceId; }
    public void setTraceId(Long traceId) { this.traceId = traceId; }
    public boolean isForceNewVersion() { return forceNewVersion; }
    public void setForceNewVersion(boolean forceNewVersion) { this.forceNewVersion = forceNewVersion; }
    public String getReimportReason() { return reimportReason; }
    public void setReimportReason(String reimportReason) { this.reimportReason = reimportReason; }

    public BomAiImportHeader getHeader() { return header; }
    public void setHeader(BomAiImportHeader header) { this.header = header; }
    public List<BomAiImportItem> getItems() { return items; }
    public void setItems(List<BomAiImportItem> items) { this.items = items; }
    public List<BomAiDocument> getDocuments() { return documents; }
    public void setDocuments(List<BomAiDocument> documents) { this.documents = documents; }
}
