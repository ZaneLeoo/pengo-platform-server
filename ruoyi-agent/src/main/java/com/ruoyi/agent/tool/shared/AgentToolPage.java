package com.ruoyi.agent.tool.shared;

/** Agent 工具分页信息。 */
public record AgentToolPage(int pageNum, int pageSize, long total, boolean hasMore)
{
}
