package com.ruoyi.agent.mapper;

import java.util.List;
import com.ruoyi.agent.domain.AgentConversation;

/**
 * Agent对话会话 数据层
 *
 * @author ruoyi
 */
public interface AgentConversationMapper
{
    /**
     * 查询会话列表（按用户ID）
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
}
