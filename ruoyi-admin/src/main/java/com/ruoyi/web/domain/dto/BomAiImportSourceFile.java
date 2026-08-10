package com.ruoyi.web.domain.dto;

import lombok.Data;

/** BOM AI 导入保存的原始图纸文件元数据。 */
@Data
public class BomAiImportSourceFile {
    private String resourceId;
    private String originalFilename;
    private String mediaType;
    private Long fileSize;
    private String fileHash;
    private String storagePath;
    private String difyFileId;
}
