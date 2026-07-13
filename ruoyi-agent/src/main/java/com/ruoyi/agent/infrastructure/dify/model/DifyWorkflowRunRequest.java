package com.ruoyi.agent.infrastructure.dify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Dify 工作流执行请求。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyWorkflowRunRequest {
    private Map<String, Object> inputs;
    private String user;

}
