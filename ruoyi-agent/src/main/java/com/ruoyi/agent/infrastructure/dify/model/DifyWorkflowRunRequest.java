package com.ruoyi.agent.infrastructure.dify.model;

import java.util.Map;

/** Dify 工作流执行请求。 */
public record DifyWorkflowRunRequest(Map<String, Object> inputs, String user) { }
