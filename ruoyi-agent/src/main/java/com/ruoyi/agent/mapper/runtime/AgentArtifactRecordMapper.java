package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentArtifactRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Agent V2 产物数据访问。 */
public interface AgentArtifactRecordMapper
{
    int insert(AgentArtifactRecord artifact);

    AgentArtifactRecord selectById(Long id);

    AgentArtifactRecord selectOwned(@Param("id") Long id, @Param("userId") Long userId);

    List<AgentArtifactRecord> selectByRunId(Long runId);

    List<AgentArtifactRecord> selectByConversationId(Long conversationId);

    int updateStatus(AgentArtifactRecord artifact);
}
