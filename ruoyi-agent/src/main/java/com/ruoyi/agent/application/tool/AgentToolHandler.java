package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import java.util.Map;

/** 一个职责明确、可由 Dify Supervisor 调用的企业工具。 */
public interface AgentToolHandler
{
    /** 稳定工具编码，与 Dify 注册名称一致。 */
    AgentToolCode code();

    /** 前端可理解的步骤类型。 */
    AgentStepType stepType();

    /** 用户可见的安全执行名称。 */
    String displayName();

    /** 在 Spring 可信上下文中执行工具。 */
    AgentToolResult execute(AgentToolContext context, Map<String, Object> input);
}
