package com.ruoyi.mes.base.domain.dto;

import com.ruoyi.mes.base.domain.ocr.BomOcrDocument;
import com.ruoyi.mes.base.domain.ocr.BomOcrIssue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** BOM OCR 导入草稿详情。 */
public class BomImportDraft
{
    /** 导入任务 ID。 */
    private Long id;

    /** 导入任务状态。 */
    private String status;

    /** 原始文件名称。 */
    private String fileName;

    /** 原始文件访问地址。 */
    private String fileUrl;

    /** 原始文件类型。 */
    private String fileType;

    /** 识别失败原因。 */
    private String errorMessage;

    /** 图纸标题区识别结果。 */
    private BomOcrDocument document;

    /** 草稿明细行。 */
    private List<BomImportDraftItem> items = new ArrayList<>();

    /** 草稿整体问题列表。 */
    private List<BomOcrIssue> issues = new ArrayList<>();

    /** 创建时间。 */
    private Date createTime;

    /** 更新时间。 */
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public BomOcrDocument getDocument() { return document; }
    public void setDocument(BomOcrDocument document) { this.document = document; }

    public List<BomImportDraftItem> getItems() { return items; }
    public void setItems(List<BomImportDraftItem> items) { this.items = items; }

    public List<BomOcrIssue> getIssues() { return issues; }
    public void setIssues(List<BomOcrIssue> issues) { this.issues = issues; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
