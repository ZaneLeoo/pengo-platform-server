package com.ruoyi.agent.infrastructure.dify.model;

import java.util.Map;

/** Dify 工作流阻塞执行结果。 */
public record DifyWorkflowRunResult(String taskId, String workflowRunId, String status, Map<String, Object> outputs,
    String error, String rawResponse) { }
