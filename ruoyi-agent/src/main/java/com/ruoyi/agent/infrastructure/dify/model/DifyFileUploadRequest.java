package com.ruoyi.agent.infrastructure.dify.model;

/** Dify 文件上传请求。 */
public record DifyFileUploadRequest(String filename, String contentType, byte[] content, String user) {
}
