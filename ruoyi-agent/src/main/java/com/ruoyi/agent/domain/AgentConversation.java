package com.ruoyi.agent.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** 本地 Agent 会话。 */
public class AgentConversation extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String difyConversationId;
    private String title;
    private String status;
    private String lastTaskId;
    private String lastWorkflowRunId;
    private Integer messageCount;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDifyConversationId() { return difyConversationId; }
    public void setDifyConversationId(String value) { this.difyConversationId = value; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastTaskId() { return lastTaskId; }
    public void setLastTaskId(String value) { this.lastTaskId = value; }
    public String getLastWorkflowRunId() { return lastWorkflowRunId; }
    public void setLastWorkflowRunId(String value) { this.lastWorkflowRunId = value; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer value) { this.messageCount = value; }
}
