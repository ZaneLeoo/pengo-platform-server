package com.ruoyi.agent.application.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.agent.domain.runtime.AgentSessionState;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 将 Spring 可信工作状态编译为 Dify Supervisor 输入变量。 */
@Component
public class AgentMemoryAssembler
{
    private final ObjectMapper objectMapper;

    public AgentMemoryAssembler(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    /** 用户输入不能覆盖保留的运行标识和会话状态。 */
    public Map<String, Object> assemble(Long runId, String runToken, AgentSessionState state,
        Map<String, Object> requestedInputs, Map<String, Object> contextRefs)
    {
        Map<String, Object> inputs = new LinkedHashMap<>();
        if (requestedInputs != null) inputs.putAll(requestedInputs);
        inputs.put("run_id", String.valueOf(runId));
        inputs.put("run_token", runToken);
        inputs.put("conversation_summary", text(state.getConversationSummary()));
        inputs.put("active_goal", text(state.getActiveGoal()));
        inputs.put("current_domain", text(state.getCurrentDomain()));
        inputs.put("last_dataset_id", id(state.getLastDatasetId()));
        inputs.put("last_artifact_id", id(state.getLastArtifactId()));
        inputs.put("last_file_id", id(state.getLastFileId()));
        inputs.put("business_context_text", serializeContext(contextRefs));
        return inputs;
    }

    /** 将结构化页面上下文序列化为 Agent 指令可稳定引用的文本。 */
    private String serializeContext(Map<String, Object> contextRefs)
    {
        try
        {
            return objectMapper.writeValueAsString(contextRefs == null ? Map.of() : contextRefs);
        }
        catch (JsonProcessingException e)
        {
            return "{}";
        }
    }

    private String text(String value)
    {
        return value == null ? "" : value;
    }

    private String id(Long value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
