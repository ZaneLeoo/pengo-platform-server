package com.ruoyi.agent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 基础 Dify 聊天请求。 */
@Data
public class AgentChatRequest {
    @NotBlank(message = "消息不能为空")
    private String query;

    private String difyConversationId;

    private Map<String, Object> inputs;

    @Valid
    @Size(max = 5, message = "单次最多上传5个附件")
    private List<AgentInputFile> files;
}
