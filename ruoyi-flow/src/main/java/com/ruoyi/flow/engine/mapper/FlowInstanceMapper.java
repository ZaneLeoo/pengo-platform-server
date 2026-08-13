package com.ruoyi.flow.engine.mapper;

import com.ruoyi.flow.engine.domain.FlowInstance;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程实例数据访问接口。
 */
@Mapper
public interface FlowInstanceMapper {

    /** 查询实例列表。 */
    List<FlowInstance> selectList(FlowInstance filter);

    /** 按ID查询实例。 */
    FlowInstance selectById(Long instanceId);

    /** 按业务查询实例（业务维度最新一条）。 */
    FlowInstance selectByBiz(String bizType, Long bizId);

    /** 新增实例。 */
    int insert(FlowInstance instance);

    /** 更新当前节点与状态。 */
    int updateCurrentNode(FlowInstance instance);

    /** 完成实例（通过/驳回/撤销，记录完成时间）。 */
    int updateFinish(FlowInstance instance);
}
