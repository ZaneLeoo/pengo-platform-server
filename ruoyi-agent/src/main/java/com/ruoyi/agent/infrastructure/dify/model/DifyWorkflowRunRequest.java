package com.ruoyi.agent.infrastructure.dify.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dify 工作流执行请求。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyWorkflowRunRequest {
    private Map<String, Object> inputs;
    private String user;
}
