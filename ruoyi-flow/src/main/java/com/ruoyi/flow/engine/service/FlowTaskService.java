package com.ruoyi.flow.engine.service;

import com.ruoyi.flow.engine.domain.FlowTask;
import com.ruoyi.flow.engine.enums.FlowTaskStatus;
import com.ruoyi.flow.engine.mapper.FlowTaskMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 审批任务查询：待办与已办列表。
 */
@Service
public class FlowTaskService {

    private final FlowTaskMapper mapper;

    public FlowTaskService(FlowTaskMapper mapper) {
        this.mapper = mapper;
    }

    /** 我的待办列表。 */
    public List<FlowTask> myTodo(String assignee) {
        FlowTask filter = new FlowTask();
        filter.setAssignee(assignee);
        filter.setStatus(FlowTaskStatus.PENDING.getCode());
        return mapper.selectList(filter);
    }

    /** 我已处理列表（同意/驳回）。 */
    public List<FlowTask> myDone(String assignee) {
        return mapper.selectDoneByAssignee(assignee);
    }

    /** 我的待办数量（角标）。 */
    public int todoCount(String assignee) {
        return mapper.countPendingByAssignee(assignee);
    }

    /** 按实例查询任务。 */
    public List<FlowTask> byInstance(Long instanceId) {
        FlowTask filter = new FlowTask();
        filter.setInstanceId(instanceId);
        return mapper.selectList(filter);
    }
}
