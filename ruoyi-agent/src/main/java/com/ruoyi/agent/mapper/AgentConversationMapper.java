package com.ruoyi.agent.mapper;

import com.ruoyi.agent.domain.AgentConversation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Agent 会话数据访问。 */
public interface AgentConversationMapper {
    /** 新增会话。 */
    int insert(AgentConversation conversation);
    /** 查询用户会话列表。 */
    List<AgentConversation> selectByUserId(Long userId);
    /** 查询用户拥有的会话。 */
    AgentConversation selectOwned(@Param("id") Long id, @Param("userId") Long userId);
    /** 更新会话。 */
    int update(AgentConversation conversation);
    /** 删除用户拥有的会话。 */
    int deleteOwned(@Param("id") Long id, @Param("userId") Long userId);
}
