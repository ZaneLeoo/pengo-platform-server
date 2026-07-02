package com.ruoyi.mes.base.domain.ocr;

import java.util.ArrayList;
import java.util.List;

/** BOM OCR 大模型结构化结果。 */
public class BomOcrResult
{
    /** 协议版本，例如 1.0。 */
    private String version;

    /** 图纸标题区/表头识别结果。 */
    private BomOcrDocument document;

    /** BOM 明细行识别结果。 */
    private List<BomOcrItem> items = new ArrayList<>();

    /** 识别或结构化过程中发现的问题。 */
    private List<BomOcrIssue> issues = new ArrayList<>();

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public BomOcrDocument getDocument() { return document; }
    public void setDocument(BomOcrDocument document) { this.document = document; }

    public List<BomOcrItem> getItems() { return items; }
    public void setItems(List<BomOcrItem> items) { this.items = items; }

    public List<BomOcrIssue> getIssues() { return issues; }
    public void setIssues(List<BomOcrIssue> issues) { this.issues = issues; }
}
