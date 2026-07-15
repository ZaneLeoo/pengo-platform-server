package com.ruoyi.agent.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 用户输入附件上传结果。 */
@Data
@AllArgsConstructor
public class AgentInputFileUploadResult {
    private String uploadFileId;
    private String name;
    private String type;
    private String mediaType;
    private Long size;
}
