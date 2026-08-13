package com.ruoyi.flow.engine.service;

import com.ruoyi.flow.engine.domain.FlowHistory;
import com.ruoyi.flow.engine.domain.FlowInstance;
import com.ruoyi.flow.engine.mapper.FlowHistoryMapper;
import com.ruoyi.flow.engine.mapper.FlowInstanceMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 流程实例查询：我发起的、实例详情与审批链。
 */
@Service
public class FlowInstanceService {

    private final FlowInstanceMapper instanceMapper;
    private final FlowHistoryMapper historyMapper;
    private final FlowEngineService engineService;

    public FlowInstanceService(FlowInstanceMapper instanceMapper, FlowHistoryMapper historyMapper,
            FlowEngineService engineService) {
        this.instanceMapper = instanceMapper;
        this.historyMapper = historyMapper;
        this.engineService = engineService;
    }

    /** 查询实例列表。 */
    public List<FlowInstance> list(FlowInstance filter) {
        return instanceMapper.selectList(filter);
    }

    /** 我发起的实例列表。 */
    public List<FlowInstance> myStarted(String submitBy) {
        FlowInstance filter = new FlowInstance();
        filter.setSubmitBy(submitBy);
        return instanceMapper.selectList(filter);
    }

    /** 实例详情（含审批链）。 */
    public FlowInstance detail(Long instanceId) {
        return engineService.getInstance(instanceId);
    }

    /** 审批链。 */
    public List<FlowHistory> history(Long instanceId) {
        return engineService.history(instanceId);
    }

    /** 按业务查询最新实例。 */
    public FlowInstance byBiz(String bizType, Long bizId) {
        return engineService.instanceByBiz(bizType, bizId);
    }
}
