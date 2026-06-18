package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.mapper.AgentConversationMapper;
import com.ruoyi.agent.service.IAgentConversationService;

/**
 * Agent会话 Service业务层处理
 *
 * @author Dylan
 */
@Service
public class AgentConversationServiceImpl implements IAgentConversationService
{
    @Autowired
    private AgentConversationMapper conversationMapper;

    @Override
    public List<AgentConversation> selectConversationList(Long userId)
    {
        return conversationMapper.selectConversationList(userId);
    }

    @Override
    public AgentConversation selectConversationById(Long id)
    {
        return conversationMapper.selectConversationById(id);
    }

    @Override
    public int insertConversation(AgentConversation conversation)
    {
        return conversationMapper.insertConversation(conversation);
    }

    @Override
    public int updateConversation(AgentConversation conversation)
    {
        return conversationMapper.updateConversation(conversation);
    }

    @Override
    public int deleteConversationById(Long id)
    {
        return conversationMapper.deleteConversationById(id);
    }

    @Override
    public AgentConversation getOrCreateConversation(Long convId, String query, String username, Long userId)
    {
        if (convId != null)
        {
            return conversationMapper.selectConversationById(convId);
        }

        // 新建会话：以query前30字作标题
        String title = query != null && query.length() > 30
                ? query.substring(0, 30) + "..."
                : query;

        AgentConversation conv = new AgentConversation();
        conv.setUserId(userId);
        conv.setTitle(title);
        conv.setStatus("0");
        conv.setMessageCount(0);
        conv.setCreateBy(username);
        conv.setCreateTime(new Date());
        conversationMapper.insertConversation(conv);
        return conv;
    }
}
