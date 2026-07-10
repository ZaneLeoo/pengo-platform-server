package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.application.artifact.AgentV2Artifact;
import com.ruoyi.agent.application.source.AgentV2Citation;
import com.ruoyi.agent.domain.enums.AgentToolResultStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 所有 Agent V2 工具必须返回的统一结果。 */
public class AgentToolResult
{
    private AgentToolResultStatus status;
    private String summary;
    private Map<String, Object> data = Collections.emptyMap();
    private List<AgentV2Artifact> artifacts = Collections.emptyList();
    private List<AgentV2Citation> citations = Collections.emptyList();
    private boolean requiresConfirmation;
    private List<String> nextActions = Collections.emptyList();
    private AgentToolError error;

    /** 创建成功结果。 */
    public static AgentToolResult success(String summary, Map<String, Object> data)
    {
        AgentToolResult result = new AgentToolResult();
        result.status = AgentToolResultStatus.SUCCEEDED;
        result.summary = summary;
        result.setData(data);
        return result;
    }

    /** 创建需要用户补充信息的结果。 */
    public static AgentToolResult clarification(String question)
    {
        AgentToolResult result = new AgentToolResult();
        result.status = AgentToolResultStatus.NEEDS_CLARIFICATION;
        result.summary = question;
        return result;
    }

    /** 创建失败结果，错误内容必须适合返回给模型。 */
    public static AgentToolResult failure(String code, String message, boolean retryable)
    {
        AgentToolResult result = new AgentToolResult();
        result.status = AgentToolResultStatus.FAILED;
        result.summary = message;
        result.error = new AgentToolError(code, message, retryable);
        return result;
    }

    public AgentToolResultStatus getStatus() { return status; }
    public String getSummary() { return summary; }
    public Map<String, Object> getData() { return data; }
    public List<AgentV2Artifact> getArtifacts() { return artifacts; }
    public List<AgentV2Citation> getCitations() { return citations; }
    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public List<String> getNextActions() { return nextActions; }
    public AgentToolError getError() { return error; }

    public void setStatus(AgentToolResultStatus status) { this.status = status; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setError(AgentToolError error) { this.error = error; }

    public void setData(Map<String, Object> data)
    {
        this.data = data == null ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    public void setArtifacts(List<AgentV2Artifact> artifacts)
    {
        this.artifacts = artifacts == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(artifacts));
    }

    public void setCitations(List<AgentV2Citation> citations)
    {
        this.citations = citations == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(citations));
    }

    public void setRequiresConfirmation(boolean requiresConfirmation)
    {
        this.requiresConfirmation = requiresConfirmation;
        if (requiresConfirmation && status == AgentToolResultStatus.SUCCEEDED)
        {
            status = AgentToolResultStatus.WAITING_CONFIRMATION;
        }
    }

    public void setNextActions(List<String> nextActions)
    {
        this.nextActions = nextActions == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(nextActions));
    }
}
