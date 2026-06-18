package com.ruoyi.agent.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Agent对话消息表 agent_message
 *
 * @author ruoyi
 */
public class AgentMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** Dify侧消息ID */
    private String difyMessageId;

    /** 角色（user/assistant） */
    private String role;

    /** 消息内容（Markdown） */
    private String content;

    /** 思考过程JSON（原始字符串，用于DB存储，序列化时忽略） */
    @JsonIgnore
    private String thinking;

    /** 元数据JSON（原始字符串，用于DB存储，序列化时忽略） */
    @JsonIgnore
    private String messageMetadata;

    /** Token消耗 */
    private Integer tokenCount;

    /** 用户反馈（1赞 0无 -1踩） */
    private String feedback;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(Long conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getDifyMessageId()
    {
        return difyMessageId;
    }

    public void setDifyMessageId(String difyMessageId)
    {
        this.difyMessageId = difyMessageId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getThinking()
    {
        return thinking;
    }

    public void setThinking(String thinking)
    {
        this.thinking = thinking;
    }

    /** 返回解析后的thinking对象给前端 */
    @JsonProperty("thinking")
    public Object getThinkingObject()
    {
        if (thinking == null || thinking.isEmpty()) return null;
        try { return JSON.parse(thinking); } catch (Exception e) { return thinking; }
    }

    public String getMessageMetadata()
    {
        return messageMetadata;
    }

    public void setMessageMetadata(String messageMetadata)
    {
        this.messageMetadata = messageMetadata;
    }

    /** 返回解析后的messageMetadata对象给前端 */
    @JsonProperty("messageMetadata")
    public Object getMessageMetadataObject()
    {
        if (messageMetadata == null || messageMetadata.isEmpty()) return null;
        try { return JSON.parse(messageMetadata); } catch (Exception e) { return messageMetadata; }
    }

    public Integer getTokenCount()
    {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount)
    {
        this.tokenCount = tokenCount;
    }

    public String getFeedback()
    {
        return feedback;
    }

    public void setFeedback(String feedback)
    {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("conversationId", getConversationId())
            .append("difyMessageId", getDifyMessageId())
            .append("role", getRole())
            .append("content", getContent())
            .append("thinking", getThinking())
            .append("messageMetadata", getMessageMetadata())
            .append("tokenCount", getTokenCount())
            .append("feedback", getFeedback())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
