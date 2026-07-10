package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentRunStep;
import java.util.List;

/** Agent V2 运行步骤数据访问。 */
public interface AgentRunStepMapper
{
    int insert(AgentRunStep step);

    AgentRunStep selectById(Long id);

    List<AgentRunStep> selectByRunId(Long runId);

    Integer selectNextSequence(Long runId);

    int update(AgentRunStep step);

    int cancelRunningByRunId(Long runId);
}
