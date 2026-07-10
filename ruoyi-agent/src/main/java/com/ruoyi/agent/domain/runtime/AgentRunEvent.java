package com.ruoyi.agent.domain.runtime;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/** 用于断线恢复和审计的 Agent V2 事件记录。 */
@Data
public class AgentRunEvent implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long runId;
    private Long sequenceNo;
    private String eventType;
    private String dataJson;
    private Date createTime;
}
