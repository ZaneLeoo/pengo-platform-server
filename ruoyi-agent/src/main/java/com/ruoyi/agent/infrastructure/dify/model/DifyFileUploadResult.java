package com.ruoyi.agent.infrastructure.dify.model;

/** Dify 文件上传结果。 */
public record DifyFileUploadResult(String id, String name, long size, String extension, String mimeType) {
}
