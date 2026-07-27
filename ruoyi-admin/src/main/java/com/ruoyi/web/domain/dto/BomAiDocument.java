package com.ruoyi.web.domain.dto;

import java.util.List;

/**
 * BOM AI 图纸识别出的单个文档/BOM。
 */
public class BomAiDocument {

    /** 原始图片或页面序号。 */
    private Integer pageNo;
    /** 母件信息。 */
    private BomAiImportHeader header;
    /** 子件明细。 */
    private List<BomAiImportItem> items;

    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }
    public BomAiImportHeader getHeader() { return header; }
    public void setHeader(BomAiImportHeader header) { this.header = header; }
    public List<BomAiImportItem> getItems() { return items; }
    public void setItems(List<BomAiImportItem> items) { this.items = items; }
}
