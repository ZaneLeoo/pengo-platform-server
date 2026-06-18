package com.ruoyi.agent.domain;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent对话消息表 agent_message
 *
 * @author Dylan
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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

    // ======== 自定义序列化方法（将JSON字符串解析为对象返回前端） ========

    /** 返回解析后的thinking对象给前端 */
    @JsonProperty("thinking")
    public Object getThinkingObject()
    {
        if (thinking == null || thinking.isEmpty()) return null;
        try { return JSON.parse(thinking); } catch (Exception e) { return thinking; }
    }

    /** 返回解析后的messageMetadata对象给前端 */
    @JsonProperty("messageMetadata")
    public Object getMessageMetadataObject()
    {
        if (messageMetadata == null || messageMetadata.isEmpty()) return null;
        try { return JSON.parse(messageMetadata); } catch (Exception e) { return messageMetadata; }
    }
}
