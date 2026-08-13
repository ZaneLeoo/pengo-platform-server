package com.ruoyi.flow.binding.mapper;

import com.ruoyi.flow.binding.domain.FlowBinding;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程绑定数据访问接口。
 */
@Mapper
public interface FlowBindingMapper {

    /** 查询绑定列表。 */
    List<FlowBinding> selectList(FlowBinding filter);

    /** 按业务类型查询绑定。 */
    FlowBinding selectByBizType(String bizType);

    /** 新增绑定。 */
    int insert(FlowBinding binding);

    /** 修改绑定流程。 */
    int update(FlowBinding binding);

    /** 删除绑定。 */
    int deleteByIds(Long[] bindingIds);
}
