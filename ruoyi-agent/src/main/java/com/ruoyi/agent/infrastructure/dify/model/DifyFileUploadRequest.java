package com.ruoyi.agent.infrastructure.dify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dify 文件上传请求。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyFileUploadRequest {
    private String filename;
    private String contentType;
    private byte[] content;
    private String user;

}
