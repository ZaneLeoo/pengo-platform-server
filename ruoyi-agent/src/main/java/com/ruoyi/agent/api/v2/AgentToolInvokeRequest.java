package com.ruoyi.agent.api.v2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import lombok.Data;

/** Dify Supervisor 调用 Spring 工具网关的统一请求。 */
@Data
public class AgentToolInvokeRequest
{
    @NotNull(message = "runId不能为空")
    private Long runId;

    @NotBlank(message = "runToken不能为空")
    private String runToken;

    private String idempotencyKey;

    private Map<String, Object> input = Collections.emptyMap();
}
