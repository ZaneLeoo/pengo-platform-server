package com.ruoyi.flow.engine.mapper;

import com.ruoyi.flow.engine.domain.FlowTask;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办任务数据访问接口。
 */
@Mapper
public interface FlowTaskMapper {

    /** 查询任务列表（按审批人或实例等条件）。 */
    List<FlowTask> selectList(FlowTask filter);

    /** 按ID查询任务。 */
    FlowTask selectById(Long taskId);

    /** 查询实例待办任务。 */
    List<FlowTask> selectPendingByInstance(Long instanceId);

    /** 查询审批人的待办任务数。 */
    int countPendingByAssignee(String assignee);

    /** 查询审批人已处理的任务（同意/驳回）。 */
    List<FlowTask> selectDoneByAssignee(String assignee);

    /** 新增任务。 */
    int insert(FlowTask task);

    /** 处理任务（同意/驳回）。 */
    int updateStatus(FlowTask task);

    /** 跳过实例的全部待办任务（驳回/撤销时清理）。 */
    int skipPendingByInstance(Long instanceId, String updateBy);
}
