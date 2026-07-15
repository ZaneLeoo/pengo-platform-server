package com.ruoyi.agent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 用户随聊天消息提交的输入附件。 */
@Data
public class AgentInputFile {
    @NotBlank(message = "Dify 文件ID不能为空")
    private String uploadFileId;

    @NotBlank(message = "附件类型不能为空")
    @Pattern(regexp = "image|document|audio|video", message = "附件类型不受支持")
    private String type;

    private String name;
    private String mediaType;
    private Long size;
}
