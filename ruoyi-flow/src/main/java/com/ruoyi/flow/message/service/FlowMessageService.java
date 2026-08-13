package com.ruoyi.flow.message.service;

import com.ruoyi.flow.message.domain.FlowMessage;
import com.ruoyi.flow.message.mapper.FlowMessageMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 流程站内消息。
 */
@Service
public class FlowMessageService {

    private final FlowMessageMapper mapper;

    public FlowMessageService(FlowMessageMapper mapper) {
        this.mapper = mapper;
    }

    /** 查询我的消息列表。 */
    public List<FlowMessage> list(String receiver, String readFlag) {
        FlowMessage filter = new FlowMessage();
        filter.setReceiver(receiver);
        filter.setReadFlag(readFlag);
        return mapper.selectByReceiver(filter);
    }

    /** 我的未读消息数。 */
    public int unreadCount(String receiver) {
        return mapper.countUnread(receiver);
    }

    /** 标记单条已读。 */
    public int read(Long messageId, String receiver) {
        FlowMessage message = new FlowMessage();
        message.setMessageId(messageId);
        message.setReceiver(receiver);
        return mapper.updateRead(message);
    }

    /** 全部标记已读。 */
    public int readAll(String receiver) {
        FlowMessage message = new FlowMessage();
        message.setReceiver(receiver);
        return mapper.updateRead(message);
    }
}
