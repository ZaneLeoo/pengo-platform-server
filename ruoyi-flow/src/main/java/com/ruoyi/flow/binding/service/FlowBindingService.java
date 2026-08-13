package com.ruoyi.flow.binding.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.flow.binding.domain.FlowBinding;
import com.ruoyi.flow.binding.mapper.FlowBindingMapper;
import com.ruoyi.flow.definition.mapper.FlowDefinitionMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 流程绑定管理：业务类型与流程定义的绑定。
 */
@Service
public class FlowBindingService {

    private final FlowBindingMapper mapper;
    private final FlowDefinitionMapper definitionMapper;

    public FlowBindingService(FlowBindingMapper mapper, FlowDefinitionMapper definitionMapper) {
        this.mapper = mapper;
        this.definitionMapper = definitionMapper;
    }

    /** 查询绑定列表。 */
    public List<FlowBinding> list(FlowBinding filter) {
        return mapper.selectList(filter);
    }

    /** 保存绑定：业务类型已存在则更新流程，否则新增。 */
    public int save(FlowBinding binding) {
        if (definitionMapper.selectById(binding.getFlowId()) == null) {
            throw new ServiceException("绑定的流程不存在");
        }
        FlowBinding existing = mapper.selectByBizType(binding.getBizType());
        if (existing != null) {
            binding.setBindingId(existing.getBindingId());
            return mapper.update(binding);
        }
        return mapper.insert(binding);
    }

    /** 删除绑定。 */
    public int remove(Long[] bindingIds) {
        return mapper.deleteByIds(bindingIds);
    }
}
