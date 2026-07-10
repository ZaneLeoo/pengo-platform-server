package com.ruoyi.agent.application.stream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.agent.api.v2.AgentV2StreamEvent;
import com.ruoyi.agent.application.run.AgentRunService;
import com.ruoyi.agent.domain.enums.AgentV2EventType;
import com.ruoyi.agent.domain.runtime.AgentRunEvent;
import com.ruoyi.agent.mapper.runtime.AgentRunEventMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 分配事件序号、持久化事件并发布实时通知。 */
@Service
public class AgentV2EventService
{
    private final AgentRunService runService;
    private final AgentRunEventMapper eventMapper;
    private final AgentV2EventBus eventBus;

    public AgentV2EventService(AgentRunService runService, AgentRunEventMapper eventMapper,
        AgentV2EventBus eventBus)
    {
        this.runService = runService;
        this.eventMapper = eventMapper;
        this.eventBus = eventBus;
    }

    /** 创建严格递增、可恢复的标准事件。 */
    @Transactional
    public AgentV2StreamEvent publish(AgentV2EventType type, Long runId, Map<String, Object> data)
    {
        long sequence = runService.nextSequence(runId);
        AgentV2StreamEvent streamEvent = AgentV2StreamEvent.of(type, runId, sequence, data);
        AgentRunEvent record = new AgentRunEvent();
        record.setRunId(runId);
        record.setSequenceNo(sequence);
        record.setEventType(type.getCode());
        record.setDataJson(JSON.toJSONString(streamEvent.data()));
        eventMapper.insert(record);
        eventBus.publish(streamEvent);
        return streamEvent;
    }

    /** 返回指定序号之后的事件，用于 SSE 重连补发。 */
    public List<AgentV2StreamEvent> listAfter(Long runId, Long sequenceNo)
    {
        long cursor = sequenceNo == null ? 0L : sequenceNo;
        return eventMapper.selectAfter(runId, cursor).stream().map(this::toStreamEvent).toList();
    }

    private AgentV2StreamEvent toStreamEvent(AgentRunEvent record)
    {
        Map<String, Object> data = JSON.parseObject(record.getDataJson(),
            new TypeReference<Map<String, Object>>() { });
        return new AgentV2StreamEvent(record.getEventType(), record.getRunId(), record.getSequenceNo(),
            record.getCreateTime().getTime(), data);
    }
}
