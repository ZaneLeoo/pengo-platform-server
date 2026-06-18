package com.ruoyi.agent.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Agent对话会话表 agent_conversation
 *
 * @author Dylan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentConversation extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** Dify侧对话ID */
    private String difyConversationId;

    /** 会话标题 */
    private String title;

    /** 状态（0进行中 1已结束） */
    private String status;

    /** 消息数量 */
    private Integer messageCount;
}
