package com.ruoyi.flow.engine.mapper;

import com.ruoyi.flow.engine.domain.FlowHistory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批历史数据访问接口。
 */
@Mapper
public interface FlowHistoryMapper {

    /** 查询实例审批链条（按时间正序）。 */
    List<FlowHistory> selectByInstance(Long instanceId);

    /** 新增历史记录。 */
    int insert(FlowHistory history);
}
