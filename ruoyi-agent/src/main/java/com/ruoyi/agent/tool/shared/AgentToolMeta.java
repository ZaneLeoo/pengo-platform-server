package com.ruoyi.agent.tool.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Agent 工具结果的数量、截断和分页信息。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolMeta {
    private Long total;
    private Boolean truncated;
    private AgentToolPage page;

    /** 创建非分页查询的元数据。 */
    public static AgentToolMeta collection(long total, boolean truncated) {
        return new AgentToolMeta(total, truncated, null);
    }

    /** 创建分页查询的元数据。 */
    public static AgentToolMeta page(int pageNum, int pageSize, long total, boolean hasMore) {
        return new AgentToolMeta(total, hasMore, new AgentToolPage(pageNum, pageSize, total, hasMore));
    }
}
