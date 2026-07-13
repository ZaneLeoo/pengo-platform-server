package com.ruoyi.agent.infrastructure.dify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dify 文件上传结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyFileUploadResult {
    private String id;
    private String name;
    private long size;
    private String extension;
    private String mimeType;

}
