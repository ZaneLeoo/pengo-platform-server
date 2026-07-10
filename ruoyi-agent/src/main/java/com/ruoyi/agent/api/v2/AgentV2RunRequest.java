package com.ruoyi.agent.api.v2;

import jakarta.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 创建 Agent V2 运行的请求。 */
@Data
public class AgentV2RunRequest
{
    private Long conversationId;

    @NotBlank(message = "消息不能为空")
    private String query;

    /** 已由 Spring 文件服务完成鉴权的附件 ID。 */
    private List<Long> attachmentIds = Collections.emptyList();

    /** 业务页面主动传入的上下文引用，例如当前 BOM ID。 */
    private Map<String, Object> contextRefs = Collections.emptyMap();

    /** Dify 应用声明的非敏感输入变量。 */
    private Map<String, Object> inputs = Collections.emptyMap();
}
