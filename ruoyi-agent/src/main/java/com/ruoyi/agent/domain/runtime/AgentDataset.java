package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 可跨轮次引用的数据查询结果。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDataset extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ownerUserId;
    private Long conversationId;
    private Long runId;
    private Long sourceToolCallId;
    private String datasetName;
    private String storageType;
    private String schemaJson;
    private String dataJson;
    private Integer rowCount;
    private Date expireTime;
}
