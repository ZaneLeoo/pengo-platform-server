package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 会话级业务工作记忆，不等同于原始聊天记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentSessionState extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long conversationId;
    private Long stateVersion;
    private String activeGoal;
    private String currentDomain;
    private String conversationSummary;
    private Long lastDatasetId;
    private Long lastArtifactId;
    private Long lastFileId;
    private String pendingActionJson;
    private String contextJson;
}
