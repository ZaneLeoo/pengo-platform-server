package com.ruoyi.flow.message.mapper;

import com.ruoyi.flow.message.domain.FlowMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程消息数据访问接口。
 */
@Mapper
public interface FlowMessageMapper {

    /** 查询接收人消息列表。 */
    List<FlowMessage> selectByReceiver(FlowMessage filter);

    /** 查询接收人未读消息数。 */
    int countUnread(String receiver);

    /** 新增消息。 */
    int insert(FlowMessage message);

    /** 标记已读（单条或全部）。 */
    int updateRead(FlowMessage message);
}
