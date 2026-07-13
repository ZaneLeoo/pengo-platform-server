package com.ruoyi.agent.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** 本地 Agent 消息。 */
public class AgentMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long conversationId;
    private String difyMessageId;
    private String taskId;
    private String workflowRunId;
    private String role;
    private String content;
    private String eventLog;
    private String metadata;
    private Integer tokenCount;
    private String status;
    private String errorMessage;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long value) {
        this.conversationId = value;
    }
    public String getDifyMessageId() {
        return difyMessageId;
    }
    public void setDifyMessageId(String value) {
        this.difyMessageId = value;
    }
    public String getTaskId() {
        return taskId;
    }
    public void setTaskId(String value) {
        this.taskId = value;
    }
    public String getWorkflowRunId() {
        return workflowRunId;
    }
    public void setWorkflowRunId(String value) {
        this.workflowRunId = value;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getEventLog() {
        return eventLog;
    }
    public void setEventLog(String value) {
        this.eventLog = value;
    }
    public String getMetadata() {
        return metadata;
    }
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public Integer getTokenCount() {
        return tokenCount;
    }
    public void setTokenCount(Integer value) {
        this.tokenCount = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }
}
