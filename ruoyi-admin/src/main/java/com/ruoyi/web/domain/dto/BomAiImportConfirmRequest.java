package com.ruoyi.web.domain.dto;

import java.util.List;

/**
 * BOM AI 导入 — 确认导入请求。
 */
public class BomAiImportConfirmRequest {

    /** 母件信息（用户编辑后） */
    private BomAiImportHeader header;
    /** 子件明细（用户编辑后） */
    private List<BomAiImportItem> items;
    /** 多个独立 BOM（用户编辑后）。 */
    private List<BomAiDocument> documents;

    public BomAiImportHeader getHeader() { return header; }
    public void setHeader(BomAiImportHeader header) { this.header = header; }
    public List<BomAiImportItem> getItems() { return items; }
    public void setItems(List<BomAiImportItem> items) { this.items = items; }
    public List<BomAiDocument> getDocuments() { return documents; }
    public void setDocuments(List<BomAiDocument> documents) { this.documents = documents; }
}
