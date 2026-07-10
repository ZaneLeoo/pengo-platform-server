package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentRunEvent;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Agent V2 可恢复事件数据访问。 */
public interface AgentRunEventMapper
{
    int insert(AgentRunEvent event);

    List<AgentRunEvent> selectAfter(@Param("runId") Long runId, @Param("sequenceNo") Long sequenceNo);
}
