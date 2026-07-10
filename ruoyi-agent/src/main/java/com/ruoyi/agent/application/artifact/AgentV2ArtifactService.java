package com.ruoyi.agent.application.artifact;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.agent.application.memory.AgentSessionStateService;
import com.ruoyi.agent.domain.enums.AgentArtifactStatus;
import com.ruoyi.agent.domain.runtime.AgentArtifactRecord;
import com.ruoyi.agent.mapper.runtime.AgentArtifactRecordMapper;
import com.ruoyi.common.exception.ServiceException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 校验、持久化并查询 Agent V2 结构化产物。 */
@Service
public class AgentV2ArtifactService
{
    private final AgentV2ArtifactValidator validator;
    private final AgentArtifactRecordMapper artifactMapper;
    private final AgentSessionStateService stateService;

    public AgentV2ArtifactService(AgentV2ArtifactValidator validator, AgentArtifactRecordMapper artifactMapper,
        AgentSessionStateService stateService)
    {
        this.validator = validator;
        this.artifactMapper = artifactMapper;
        this.stateService = stateService;
    }

    /** 保存通过白名单校验的产物并更新会话最近产物引用。 */
    @Transactional
    public AgentArtifactRecord create(Long conversationId, Long runId, Long messageId, AgentV2Artifact artifact,
        String username)
    {
        if (!validator.isValid(artifact))
        {
            throw new ServiceException("Agent产物结构不安全或不完整");
        }
        AgentArtifactRecord record = new AgentArtifactRecord();
        record.setConversationId(conversationId);
        record.setRunId(runId);
        record.setMessageId(messageId);
        record.setDatasetId(artifact.getDatasetId());
        record.setArtifactType(artifact.getType().toUpperCase());
        record.setVersion(artifact.getVersion());
        record.setTitle(artifact.getTitle());
        record.setStatus(AgentArtifactStatus.READY.name());
        record.setPayloadJson(JSON.toJSONString(artifact.getPayload()));
        record.setFileId(artifact.getFileId());
        record.setMimeType(artifact.getMimeType());
        record.setPreviewUrl(artifact.getPreviewUrl());
        record.setDownloadUrl(artifact.getDownloadUrl());
        record.setCreateBy(username);
        record.setUpdateBy(username);
        artifactMapper.insert(record);
        artifact.setArtifactId(record.getId());
        stateService.rememberArtifact(conversationId, record.getId(), record.getFileId(), username);
        return record;
    }

    /** 查询某次运行产生的全部产物。 */
    public List<AgentArtifactRecord> listByRunId(Long runId)
    {
        return artifactMapper.selectByRunId(runId);
    }

    /** 将持久化记录投影为与实时事件完全一致的前端协议。 */
    public List<AgentV2Artifact> listViewsByRunId(Long runId)
    {
        return listByRunId(runId).stream().map(this::toArtifact).toList();
    }

    /** 按当前用户校验并读取产物。 */
    public AgentArtifactRecord requireOwned(Long artifactId, Long userId)
    {
        AgentArtifactRecord record = artifactMapper.selectOwned(artifactId, userId);
        if (record == null)
        {
            throw new ServiceException("产物不存在或无权访问");
        }
        return record;
    }

    private AgentV2Artifact toArtifact(AgentArtifactRecord record)
    {
        AgentV2Artifact artifact = new AgentV2Artifact();
        artifact.setArtifactId(record.getId());
        artifact.setType(record.getArtifactType());
        artifact.setVersion(record.getVersion());
        artifact.setTitle(record.getTitle());
        artifact.setDatasetId(record.getDatasetId());
        Map<String, Object> payload = record.getPayloadJson() == null
            ? Map.of() : JSON.parseObject(record.getPayloadJson(), new TypeReference<Map<String, Object>>() { });
        artifact.setPayload(payload);
        artifact.setFileId(record.getFileId());
        artifact.setMimeType(record.getMimeType());
        artifact.setPreviewUrl(record.getPreviewUrl());
        artifact.setDownloadUrl(record.getDownloadUrl());
        return artifact;
    }
}
