package com.ruoyi.flow.definition.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.flow.definition.domain.FlowDefinition;
import com.ruoyi.flow.definition.domain.FlowDefinitionNode;
import com.ruoyi.flow.definition.mapper.FlowDefinitionMapper;
import com.ruoyi.flow.definition.mapper.FlowDefinitionNodeMapper;
import com.ruoyi.flow.engine.domain.FlowInstance;
import com.ruoyi.flow.engine.mapper.FlowInstanceMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程定义管理：定义 CRUD、节点配置与启停。
 */
@Service
public class FlowDefinitionService {

    private final FlowDefinitionMapper mapper;
    private final FlowDefinitionNodeMapper nodeMapper;
    private final FlowInstanceMapper instanceMapper;

    public FlowDefinitionService(FlowDefinitionMapper mapper, FlowDefinitionNodeMapper nodeMapper,
            FlowInstanceMapper instanceMapper) {
        this.mapper = mapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
    }

    /** 查询流程定义列表。 */
    public List<FlowDefinition> list(FlowDefinition filter) {
        return mapper.selectList(filter);
    }

    /** 查询流程定义详细（含节点）。 */
    public FlowDefinition get(Long flowId) {
        FlowDefinition definition = mapper.selectById(flowId);
        if (definition == null) {
            throw new ServiceException("流程定义不存在");
        }
        return definition;
    }

    /** 查询流程节点列表。 */
    public List<FlowDefinitionNode> nodes(Long flowId) {
        return nodeMapper.selectByFlowId(flowId);
    }

    /** 新增流程定义（草稿状态）。 */
    public int add(FlowDefinition definition) {
        if (mapper.selectByKey(definition.getFlowKey()) != null) {
            throw new ServiceException("流程编码已存在");
        }
        definition.setStatus("0");
        return mapper.insert(definition);
    }

    /** 修改流程定义基本信息。 */
    public int edit(FlowDefinition definition) {
        FlowDefinition old = get(definition.getFlowId());
        if ("1".equals(old.getStatus())) {
            throw new ServiceException("流程已启用，请先停用再修改");
        }
        if (mapper.selectByKey(definition.getFlowKey()) != null
                && !mapper.selectByKey(definition.getFlowKey()).getFlowId().equals(definition.getFlowId())) {
            throw new ServiceException("流程编码已存在");
        }
        return mapper.update(definition);
    }

    /** 整体保存节点链（线性顺序）。 */
    @Transactional
    public void saveNodes(Long flowId, List<FlowDefinitionNode> nodes) {
        FlowDefinition definition = get(flowId);
        if ("1".equals(definition.getStatus())) {
            throw new ServiceException("流程已启用，请先停用再配置节点");
        }
        nodeMapper.deleteByFlowId(flowId);
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            FlowDefinitionNode node = nodes.get(i);
            node.setNodeId(null);
            node.setFlowId(flowId);
            node.setSortOrder(i + 1);
            nodeMapper.insert(node);
        }
    }

    /** 启用/停用流程。 */
    public int updateStatus(FlowDefinition definition) {
        FlowDefinition old = get(definition.getFlowId());
        if ("1".equals(definition.getStatus())) {
            if (nodeMapper.selectByFlowId(definition.getFlowId()).isEmpty()) {
                throw new ServiceException("请先配置审批节点再启用");
            }
        }
        return mapper.updateStatus(definition);
    }

    /** 删除流程定义（存在流程实例时禁止删除）。 */
    @Transactional
    public int remove(Long[] flowIds) {
        for (Long flowId : flowIds) {
            FlowInstance filter = new FlowInstance();
            filter.setFlowId(flowId);
            if (!instanceMapper.selectList(filter).isEmpty()) {
                FlowDefinition definition = mapper.selectById(flowId);
                throw new ServiceException("流程【" + (definition == null ? flowId : definition.getFlowName())
                        + "】已有审批记录，不能删除");
            }
        }
        for (Long flowId : flowIds) {
            nodeMapper.deleteByFlowId(flowId);
        }
        return mapper.deleteByIds(flowIds);
    }
}
