package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.domain.runtime.AgentRun;
import com.ruoyi.agent.domain.runtime.AgentSessionState;

/** Spring 为工具提供的可信运行上下文，禁止从模型参数中构造。 */
public record AgentToolContext(AgentRun run, AgentSessionState sessionState, String username)
{
    public Long runId() { return run.getId(); }
    public Long conversationId() { return run.getConversationId(); }
    public Long userId() { return run.getUserId(); }
}
