package com.ruoyi.agent.application.tool;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.application.artifact.AgentV2Artifact;
import com.ruoyi.agent.application.memory.AgentSessionStateService;
import com.ruoyi.agent.application.run.AgentRunService;
import com.ruoyi.agent.application.stream.AgentV2EventService;
import com.ruoyi.agent.domain.enums.AgentRunStatus;
import com.ruoyi.agent.domain.enums.AgentStepStatus;
import com.ruoyi.agent.domain.enums.AgentToolCallStatus;
import com.ruoyi.agent.domain.enums.AgentToolResultStatus;
import com.ruoyi.agent.domain.enums.AgentV2EventType;
import com.ruoyi.agent.domain.runtime.AgentRun;
import com.ruoyi.agent.domain.runtime.AgentRunStep;
import com.ruoyi.agent.domain.runtime.AgentToolCall;
import com.ruoyi.agent.mapper.runtime.AgentRunStepMapper;
import com.ruoyi.agent.mapper.runtime.AgentToolCallMapper;
import com.ruoyi.common.exception.ServiceException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 统一执行工具并生成 Step、ToolCall、Artifact 与 SSE 事件。 */
@Service
public class AgentToolExecutionService
{
    private static final Logger log = LoggerFactory.getLogger(AgentToolExecutionService.class);
    private static final int MAX_INPUT_JSON_LENGTH = 100_000;

    private final AgentRunService runService;
    private final AgentSessionStateService stateService;
    private final AgentToolRegistry registry;
    private final AgentRunStepMapper stepMapper;
    private final AgentToolCallMapper toolCallMapper;
    private final AgentV2EventService eventService;

    public AgentToolExecutionService(AgentRunService runService, AgentSessionStateService stateService,
        AgentToolRegistry registry, AgentRunStepMapper stepMapper, AgentToolCallMapper toolCallMapper,
        AgentV2EventService eventService)
    {
        this.runService = runService;
        this.stateService = stateService;
        this.registry = registry;
        this.stepMapper = stepMapper;
        this.toolCallMapper = toolCallMapper;
        this.eventService = eventService;
    }

    /** 执行 Dify 请求的工具；幂等键命中成功记录时直接复用结果。 */
    @Transactional
    public AgentToolResult execute(Long runId, String toolToken, String toolCode, Map<String, Object> input,
        String idempotencyKey)
    {
        AgentRun run = runService.requireToolAuthorized(runId, toolToken);
        AgentToolCall previous = idempotencyKey == null ? null : toolCallMapper.selectByIdempotencyKey(idempotencyKey);
        if (previous != null)
        {
            if (!runId.equals(previous.getRunId()))
            {
                throw new ServiceException("工具幂等键与当前运行不匹配");
            }
            if (AgentToolCallStatus.SUCCEEDED.name().equals(previous.getStatus()))
            {
                return JSON.parseObject(previous.getOutputJson(), AgentToolResult.class);
            }
            throw new ServiceException("同一工具调用正在执行或已失败");
        }

        AgentToolHandler handler = registry.require(toolCode);
        Map<String, Object> safeInput = input == null ? Map.of() : new LinkedHashMap<>(input);
        String inputJson = JSON.toJSONString(safeInput);
        if (inputJson.length() > MAX_INPUT_JSON_LENGTH)
        {
            throw new ServiceException("工具输入过大，请使用数据集或文件引用");
        }

        Date startedAt = new Date();
        AgentRunStep step = startStep(run, handler, startedAt);
        AgentToolCall toolCall = startToolCall(run, step, handler, inputJson, idempotencyKey, startedAt);
        publishStarted(runId, step, toolCall, handler);

        try
        {
            AgentToolContext context = new AgentToolContext(run, stateService.get(run.getConversationId()),
                run.getCreateBy());
            AgentToolResult result = handler.execute(context, safeInput);
            if (result == null)
            {
                throw new ServiceException("Agent工具未返回结果");
            }
            finishSuccess(runId, step, toolCall, handler, result);
            return result;
        }
        catch (RuntimeException e)
        {
            log.error("Agent工具执行异常, runId={}, toolCode={}", runId, toolCode, e);
            AgentToolResult failure = AgentToolResult.failure("TOOL_EXECUTION_FAILED", safeMessage(e), false);
            finishFailure(runId, step, toolCall, handler, failure);
            return failure;
        }
    }

    private AgentRunStep startStep(AgentRun run, AgentToolHandler handler, Date startedAt)
    {
        AgentRunStep step = new AgentRunStep();
        step.setRunId(run.getId());
        step.setSequenceNo(stepMapper.selectNextSequence(run.getId()));
        step.setStepType(handler.stepType().name());
        step.setDisplayName(handler.displayName());
        step.setStatus(AgentStepStatus.RUNNING.name());
        step.setStartedAt(startedAt);
        step.setCreateBy(run.getCreateBy());
        step.setUpdateBy(run.getCreateBy());
        stepMapper.insert(step);
        return step;
    }

    private AgentToolCall startToolCall(AgentRun run, AgentRunStep step, AgentToolHandler handler,
        String inputJson, String idempotencyKey, Date startedAt)
    {
        AgentToolCall toolCall = new AgentToolCall();
        toolCall.setRunId(run.getId());
        toolCall.setStepId(step.getId());
        toolCall.setToolCode(handler.code().getCode());
        toolCall.setStatus(AgentToolCallStatus.RUNNING.name());
        toolCall.setInputJson(inputJson);
        toolCall.setIdempotencyKey(idempotencyKey);
        toolCall.setConfirmationRequired(0);
        toolCall.setStartedAt(startedAt);
        toolCall.setCreateBy(run.getCreateBy());
        toolCall.setUpdateBy(run.getCreateBy());
        toolCallMapper.insert(toolCall);
        return toolCall;
    }

    private void publishStarted(Long runId, AgentRunStep step, AgentToolCall toolCall, AgentToolHandler handler)
    {
        eventService.publish(AgentV2EventType.STEP_STARTED, runId, Map.of(
            "stepId", step.getId(), "stepType", step.getStepType(), "displayName", step.getDisplayName()));
        eventService.publish(AgentV2EventType.TOOL_STARTED, runId, Map.of(
            "toolCallId", toolCall.getId(), "toolCode", handler.code().getCode(), "stepId", step.getId()));
    }

    private void finishSuccess(Long runId, AgentRunStep step, AgentToolCall toolCall, AgentToolHandler handler,
        AgentToolResult result)
    {
        Date finishedAt = new Date();
        long duration = Duration.between(step.getStartedAt().toInstant(), finishedAt.toInstant()).toMillis();
        boolean waiting = result.getStatus() == AgentToolResultStatus.WAITING_CONFIRMATION;
        toolCall.setStatus(waiting ? AgentToolCallStatus.WAITING_CONFIRMATION.name()
            : AgentToolCallStatus.SUCCEEDED.name());
        toolCall.setOutputJson(JSON.toJSONString(result));
        toolCall.setConfirmationRequired(result.isRequiresConfirmation() ? 1 : 0);
        toolCall.setFinishedAt(finishedAt);
        toolCall.setDurationMs(duration);
        toolCallMapper.update(toolCall);

        step.setStatus(waiting ? AgentStepStatus.PENDING.name() : AgentStepStatus.COMPLETED.name());
        step.setSummary(result.getSummary());
        step.setFinishedAt(finishedAt);
        step.setDurationMs(duration);
        stepMapper.update(step);

        eventService.publish(AgentV2EventType.TOOL_COMPLETED, runId, Map.of(
            "toolCallId", toolCall.getId(), "toolCode", handler.code().getCode(),
            "summary", nullToEmpty(result.getSummary()), "durationMs", duration));
        eventService.publish(AgentV2EventType.STEP_COMPLETED, runId, Map.of(
            "stepId", step.getId(), "stepType", step.getStepType(),
            "summary", nullToEmpty(result.getSummary()), "durationMs", duration));
        for (AgentV2Artifact artifact : result.getArtifacts())
        {
            eventService.publish(AgentV2EventType.ARTIFACT_CREATED, runId, artifactData(artifact));
        }
        if (waiting)
        {
            eventService.publish(AgentV2EventType.APPROVAL_REQUIRED, runId,
                Map.of("toolCallId", toolCall.getId(), "summary", nullToEmpty(result.getSummary())));
        }
    }

    private void finishFailure(Long runId, AgentRunStep step, AgentToolCall toolCall, AgentToolHandler handler,
        AgentToolResult failure)
    {
        Date finishedAt = new Date();
        long duration = Duration.between(step.getStartedAt().toInstant(), finishedAt.toInstant()).toMillis();
        toolCall.setStatus(AgentToolCallStatus.FAILED.name());
        toolCall.setOutputJson(JSON.toJSONString(failure));
        toolCall.setFinishedAt(finishedAt);
        toolCall.setDurationMs(duration);
        toolCall.setErrorCode(failure.getError().code());
        toolCall.setErrorMessage(failure.getError().message());
        toolCallMapper.update(toolCall);

        step.setStatus(AgentStepStatus.FAILED.name());
        step.setSummary(failure.getSummary());
        step.setFinishedAt(finishedAt);
        step.setDurationMs(duration);
        stepMapper.update(step);
        eventService.publish(AgentV2EventType.TOOL_FAILED, runId, Map.of(
            "toolCallId", toolCall.getId(), "toolCode", handler.code().getCode(),
            "errorCode", failure.getError().code(), "message", failure.getError().message()));
        eventService.publish(AgentV2EventType.STEP_FAILED, runId, Map.of(
            "stepId", step.getId(), "stepType", step.getStepType(), "message", failure.getError().message()));
    }

    private Map<String, Object> artifactData(AgentV2Artifact artifact)
    {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("artifactId", artifact.getArtifactId());
        value.put("type", artifact.getType());
        value.put("version", artifact.getVersion());
        value.put("title", artifact.getTitle());
        value.put("datasetId", artifact.getDatasetId());
        value.put("payload", artifact.getPayload());
        value.put("fileId", artifact.getFileId());
        value.put("mimeType", artifact.getMimeType());
        value.put("previewUrl", artifact.getPreviewUrl());
        value.put("downloadUrl", artifact.getDownloadUrl());
        value.values().removeIf(java.util.Objects::isNull);
        return value;
    }

    private String safeMessage(RuntimeException error)
    {
        if (error instanceof ServiceException && error.getMessage() != null)
        {
            return error.getMessage();
        }
        return "工具执行失败，请稍后重试";
    }

    private String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }
}
