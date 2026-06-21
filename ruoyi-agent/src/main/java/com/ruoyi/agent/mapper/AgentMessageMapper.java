package com.ruoyi.agent.mapper;

import com.ruoyi.agent.domain.AgentMessage;
import java.util.List;

/** Agent 消息数据访问。 */
public interface AgentMessageMapper
{
    /** 新增消息。 */
    int insert(AgentMessage message);
    /** 更新消息。 */
    int update(AgentMessage message);
    /** 查询会话消息。 */
    List<AgentMessage> selectByConversationId(Long conversationId);
    /** 删除会话消息。 */
    int deleteByConversationId(Long conversationId);
}
