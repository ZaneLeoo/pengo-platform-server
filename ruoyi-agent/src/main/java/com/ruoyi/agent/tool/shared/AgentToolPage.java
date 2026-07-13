package com.ruoyi.agent.tool.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Agent 工具分页信息。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolPage {
    private int pageNum;
    private int pageSize;
    private long total;
    private boolean hasMore;

}
