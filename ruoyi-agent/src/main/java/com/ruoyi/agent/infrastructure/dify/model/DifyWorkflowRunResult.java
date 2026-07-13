package com.ruoyi.agent.infrastructure.dify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Dify 工作流阻塞执行结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyWorkflowRunResult {
    private String taskId;
    private String workflowRunId;
    private String status;
    private Map<String, Object> outputs;
    private String error;
    private String rawResponse;

}
