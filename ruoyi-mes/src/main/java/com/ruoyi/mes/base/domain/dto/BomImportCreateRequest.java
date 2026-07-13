package com.ruoyi.mes.base.domain.dto;

import com.ruoyi.mes.base.domain.ocr.BomOcrResult;

/** 创建 BOM OCR 导入草稿请求。 */
public class BomImportCreateRequest {
    /** 原始文件名称。 */
    private String fileName;

    /** 原始文件访问地址。 */
    private String fileUrl;

    /** 原始文件类型，例如 image、pdf。 */
    private String fileType;

    /** Dify/大模型结构化结果。 */
    private BomOcrResult result;

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

    public BomOcrResult getResult() {
        return result;
    }
    public void setResult(BomOcrResult result) {
        this.result = result;
    }
}
