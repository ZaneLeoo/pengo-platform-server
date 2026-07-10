package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Agent V2 中可审计、可展示的执行步骤。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentRunStep extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long runId;
    private Integer sequenceNo;
    private String stepType;
    private String displayName;
    private String status;
    private String summary;
    private Date startedAt;
    private Date finishedAt;
    private Long durationMs;
}
