package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.AgentMessage;

/**
 * Agent消息 Service接口
 *
 * @author ruoyi
 */
public interface IAgentMessageService
{
    /**
     * 查询会话的消息列表
     *
     * @param conversationId 会话ID
     * @return 消息集合
     */
    public List<AgentMessage> selectMessagesByConversationId(Long conversationId);

    /**
     * 新增消息
     *
     * @param message 消息信息
     * @return 结果
     */
    public int insertMessage(AgentMessage message);

    /**
     * 更新用户反馈
     *
     * @param id 消息ID
     * @param feedback 反馈
     * @return 结果
     */
    public int updateFeedback(Long id, String feedback);

    /**
     * 删除会话下所有消息
     *
     * @param conversationId 会话ID
     * @return 结果
     */
    public int deleteMessagesByConversationId(Long conversationId);
}
