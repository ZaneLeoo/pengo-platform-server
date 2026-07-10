package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Supervisor 发起的一次工具调用记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentToolCall extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long runId;
    private Long stepId;
    private String externalCallId;
    private String toolCode;
    private String status;
    private String inputJson;
    private String outputJson;
    private String idempotencyKey;
    private Integer confirmationRequired;
    private Date startedAt;
    private Date finishedAt;
    private Long durationMs;
    private String errorCode;
    private String errorMessage;
}
