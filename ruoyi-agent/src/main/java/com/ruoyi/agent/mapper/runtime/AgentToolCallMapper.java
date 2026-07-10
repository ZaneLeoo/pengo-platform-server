package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentToolCall;
import java.util.List;

/** Agent V2 工具调用数据访问。 */
public interface AgentToolCallMapper
{
    int insert(AgentToolCall toolCall);

    AgentToolCall selectById(Long id);

    AgentToolCall selectByIdempotencyKey(String idempotencyKey);

    List<AgentToolCall> selectByRunId(Long runId);

    int update(AgentToolCall toolCall);

    int cancelRunningByRunId(Long runId);
}
