package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.domain.enums.AgentStepType;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.common.exception.ServiceException;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 当目标或参数不明确时要求 Supervisor 停止猜测并询问用户。 */
@Component
public class RequestClarificationTool implements AgentToolHandler
{
    @Override
    public AgentToolCode code() { return AgentToolCode.REQUEST_CLARIFICATION; }

    @Override
    public AgentStepType stepType() { return AgentStepType.CLARIFICATION; }

    @Override
    public String displayName() { return "需要补充信息"; }

    @Override
    public AgentToolResult execute(AgentToolContext context, Map<String, Object> input)
    {
        Object value = input.get("question");
        if (!(value instanceof String question) || question.isBlank())
        {
            throw new ServiceException("澄清问题不能为空");
        }
        return AgentToolResult.clarification(question.trim());
    }
}
