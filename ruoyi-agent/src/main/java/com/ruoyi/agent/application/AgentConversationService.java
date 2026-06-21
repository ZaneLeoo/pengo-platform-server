package com.ruoyi.agent.application;

import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.domain.enums.ConversationStatus;
import com.ruoyi.agent.mapper.AgentConversationMapper;
import com.ruoyi.agent.mapper.AgentMessageMapper;
import com.ruoyi.common.exception.ServiceException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 本地会话业务，统一执行用户归属校验。 */
@Service
public class AgentConversationService
{
    private final AgentConversationMapper conversationMapper;
    private final AgentMessageMapper messageMapper;
    private final ConversationTitlePolicy titlePolicy;

    public AgentConversationService(AgentConversationMapper conversationMapper, AgentMessageMapper messageMapper,
        ConversationTitlePolicy titlePolicy)
    {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.titlePolicy = titlePolicy;
    }

    /** 创建当前用户的本地会话。 */
    public AgentConversation create(Long userId, String title, String username)
    {
        AgentConversation conversation = new AgentConversation();
        conversation.setUserId(userId);
        conversation.setTitle(titlePolicy.fromQuery(title));
        conversation.setStatus(ConversationStatus.ACTIVE.getCode());
        conversation.setMessageCount(0);
        conversation.setCreateBy(username);
        conversationMapper.insert(conversation);
        return conversation;
    }

    /** 查询当前用户的会话列表。 */
    public List<AgentConversation> list(Long userId)
    {
        return conversationMapper.selectByUserId(userId);
    }

    /** 查询当前用户指定会话。 */
    public AgentConversation requireOwned(Long id, Long userId)
    {
        AgentConversation conversation = conversationMapper.selectOwned(id, userId);
        if (conversation == null)
        {
            throw new ServiceException("会话不存在或无权访问");
        }
        return conversation;
    }

    /** 查询当前用户指定会话的消息。 */
    public List<AgentMessage> messages(Long id, Long userId)
    {
        requireOwned(id, userId);
        return messageMapper.selectByConversationId(id);
    }

    /** 修改当前用户指定会话的标题。 */
    public void rename(Long id, Long userId, String title, String username)
    {
        AgentConversation conversation = requireOwned(id, userId);
        conversation.setTitle(titlePolicy.fromQuery(title));
        conversation.setUpdateBy(username);
        conversationMapper.update(conversation);
    }

    /** 删除当前用户指定会话及其本地消息。 */
    @Transactional
    public void delete(Long id, Long userId)
    {
        requireOwned(id, userId);
        messageMapper.deleteByConversationId(id);
        conversationMapper.deleteOwned(id, userId);
    }
}
