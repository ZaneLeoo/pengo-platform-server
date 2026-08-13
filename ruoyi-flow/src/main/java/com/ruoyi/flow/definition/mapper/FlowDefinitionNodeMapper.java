package com.ruoyi.flow.definition.mapper;

import com.ruoyi.flow.definition.domain.FlowDefinitionNode;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程节点数据访问接口。
 */
@Mapper
public interface FlowDefinitionNodeMapper {

    /** 按流程查询节点（按顺序）。 */
    List<FlowDefinitionNode> selectByFlowId(Long flowId);

    /** 新增节点。 */
    int insert(FlowDefinitionNode node);

    /** 修改节点。 */
    int update(FlowDefinitionNode node);

    /** 删除流程全部节点（重建时使用）。 */
    int deleteByFlowId(Long flowId);

    /** 删除指定节点。 */
    int deleteByIds(Long[] nodeIds);
}
