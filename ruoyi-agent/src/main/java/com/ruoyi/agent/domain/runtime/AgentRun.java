package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 一次用户请求对应的 Agent V2 运行实例。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentRun extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long conversationId;
    private Long userId;
    private Long userMessageId;
    private Long assistantMessageId;
    private String status;
    private String activeAgent;
    private String difyTaskId;
    private String workflowRunId;
    private String toolTokenHash;
    private Date toolTokenExpireAt;
    private Long lastSequence;
    private Integer tokenCount;
    private Date startedAt;
    private Date finishedAt;
    private String errorCode;
    private String errorMessage;
}
