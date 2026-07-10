package com.ruoyi.agent.mapper.runtime;

import com.ruoyi.agent.domain.runtime.AgentDataset;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Agent V2 数据集数据访问。 */
public interface AgentDatasetMapper
{
    int insert(AgentDataset dataset);

    AgentDataset selectById(Long id);

    AgentDataset selectOwned(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    List<AgentDataset> selectByConversationId(Long conversationId);

    int deleteExpired();
}
