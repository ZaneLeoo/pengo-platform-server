package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentRun;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Agent V2 运行实例数据访问。 */
public interface AgentRunMapper
{
    int insert(AgentRun run);

    AgentRun selectById(Long id);

    AgentRun selectOwned(@Param("id") Long id, @Param("userId") Long userId);

    AgentRun selectForUpdate(Long id);

    List<AgentRun> selectByConversationId(Long conversationId);

    int update(AgentRun run);
}
