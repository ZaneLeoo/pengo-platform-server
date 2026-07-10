package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentSessionState;

/** Agent V2 会话工作状态数据访问。 */
public interface AgentSessionStateMapper
{
    AgentSessionState selectByConversationId(Long conversationId);

    AgentSessionState selectForUpdate(Long conversationId);

    int insert(AgentSessionState state);

    int updateWithVersion(AgentSessionState state);

    int deleteByConversationId(Long conversationId);
}
