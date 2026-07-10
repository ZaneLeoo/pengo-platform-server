package com.ruoyi.agent.application.run;

import com.ruoyi.agent.domain.runtime.AgentRun;

/** 新运行及仅在编排期间使用的明文工具令牌。 */
public record AgentRunHandle(AgentRun run, String toolToken)
{
}
