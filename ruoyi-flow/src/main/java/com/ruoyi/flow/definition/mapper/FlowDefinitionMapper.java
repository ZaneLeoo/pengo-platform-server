package com.ruoyi.flow.definition.mapper;

import com.ruoyi.flow.definition.domain.FlowDefinition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义数据访问接口。
 */
@Mapper
public interface FlowDefinitionMapper {

    /** 查询流程定义列表。 */
    List<FlowDefinition> selectList(FlowDefinition filter);

    /** 按ID查询流程定义。 */
    FlowDefinition selectById(Long flowId);

    /** 按编码查询流程定义。 */
    FlowDefinition selectByKey(String flowKey);

    /** 新增流程定义。 */
    int insert(FlowDefinition definition);

    /** 修改流程定义。 */
    int update(FlowDefinition definition);

    /** 更新流程状态（启用/停用）。 */
    int updateStatus(FlowDefinition definition);

    /** 批量删除流程定义。 */
    int deleteByIds(Long[] flowIds);
}
