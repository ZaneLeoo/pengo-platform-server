package com.ruoyi.agent.domain.runtime;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Agent V2 结构化产物持久化记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentArtifactRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long conversationId;
    private Long runId;
    private Long messageId;
    private Long datasetId;
    private String artifactType;
    private String version;
    private String title;
    private String status;
    private String payloadJson;
    private Long fileId;
    private String mimeType;
    private String previewUrl;
    private String downloadUrl;
}
