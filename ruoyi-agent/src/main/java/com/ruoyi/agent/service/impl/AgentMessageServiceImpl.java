package com.ruoyi.agent.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.mapper.AgentMessageMapper;
import com.ruoyi.agent.service.IAgentMessageService;

/**
 * Agent消息 Service业务层处理
 *
 * @author Dylan
 */
@Service
public class AgentMessageServiceImpl implements IAgentMessageService
{
    @Autowired
    private AgentMessageMapper messageMapper;

    @Override
    public List<AgentMessage> selectMessagesByConversationId(Long conversationId)
    {
        return messageMapper.selectMessagesByConversationId(conversationId);
    }

    @Override
    public int insertMessage(AgentMessage message)
    {
        return messageMapper.insertMessage(message);
    }

    @Override
    public int updateFeedback(Long id, String feedback)
    {
        return messageMapper.updateFeedback(id, feedback);
    }

    @Override
    public int deleteMessagesByConversationId(Long conversationId)
    {
        return messageMapper.deleteMessagesByConversationId(conversationId);
    }
}
