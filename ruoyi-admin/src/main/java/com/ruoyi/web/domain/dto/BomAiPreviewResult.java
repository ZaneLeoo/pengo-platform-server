package com.ruoyi.web.domain.dto;

import java.util.List;

/**
 * BOM AI 导入 — 预览结果。
 */
public class BomAiPreviewResult {

    /** 母件信息 */
    private BomAiImportHeader header;
    /** 子件明细 */
    private List<BomAiImportItem> items;
    /** 是否识别成功 */
    private boolean success;
    /** 错误信息 */
    private String error;

    public BomAiImportHeader getHeader() { return header; }
    public void setHeader(BomAiImportHeader header) { this.header = header; }
    public List<BomAiImportItem> getItems() { return items; }
    public void setItems(List<BomAiImportItem> items) { this.items = items; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
