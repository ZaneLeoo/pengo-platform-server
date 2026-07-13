package com.ruoyi.agent.tool.shared;

/** Agent 工具结果的数量、截断和分页信息。 */
public record AgentToolMeta(Long total, Boolean truncated, AgentToolPage page)
{
    /** 创建非分页查询的元数据。 */
    public static AgentToolMeta collection(long total, boolean truncated)
    {
        return new AgentToolMeta(total, truncated, null);
    }

    /** 创建分页查询的元数据。 */
    public static AgentToolMeta page(int pageNum, int pageSize, long total, boolean hasMore)
    {
        return new AgentToolMeta(total, hasMore, new AgentToolPage(pageNum, pageSize, total, hasMore));
    }
}
