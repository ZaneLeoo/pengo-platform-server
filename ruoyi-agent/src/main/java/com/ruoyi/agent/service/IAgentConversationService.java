package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.AgentConversation;

/**
 * Agent会话 Service接口
 *
 * @author ruoyi
 */
public interface IAgentConversationService
{
    /**
     * 查询会话列表
     *
     * @param userId 用户ID
     * @return 会话集合
     */
    public List<AgentConversation> selectConversationList(Long userId);

    /**
     * 查询会话详情
     *
     * @param id 会话ID
     * @return 会话信息
     */
    public AgentConversation selectConversationById(Long id);

    /**
     * 新增会话
     *
     * @param conversation 会话信息
     * @return 结果
     */
    public int insertConversation(AgentConversation conversation);

    /**
     * 修改会话
     *
     * @param conversation 会话信息
     * @return 结果
     */
    public int updateConversation(AgentConversation conversation);

    /**
     * 删除会话
     *
     * @param id 会话ID
     * @return 结果
     */
    public int deleteConversationById(Long id);

    /**
     * 获取或创建会话（新会话时自动以query前30字作标题）
     *
     * @param convId   会话ID（null则新建）
     * @param query    用户首条问题（用作标题）
     * @param username 当前用户名
     * @param userId   当前用户ID
     * @return 会话对象
     */
    public AgentConversation getOrCreateConversation(Long convId, String query, String username, Long userId);
}
