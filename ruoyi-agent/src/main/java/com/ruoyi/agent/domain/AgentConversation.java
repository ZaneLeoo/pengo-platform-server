package com.ruoyi.agent.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

import java.io.Serial;

/**
 * Agent对话会话表 agent_conversation
 *
 * @author Dylan
 */
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

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getDifyConversationId()
    {
        return difyConversationId;
    }

    public void setDifyConversationId(String difyConversationId)
    {
        this.difyConversationId = difyConversationId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getMessageCount()
    {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount)
    {
        this.messageCount = messageCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("difyConversationId", getDifyConversationId())
            .append("title", getTitle())
            .append("status", getStatus())
            .append("messageCount", getMessageCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
