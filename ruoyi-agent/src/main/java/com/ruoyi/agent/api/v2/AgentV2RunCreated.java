package com.ruoyi.agent.api.v2;

/** 创建运行后返回给前端的稳定标识。 */
public record AgentV2RunCreated(Long runId, Long conversationId, String status)
{
}
