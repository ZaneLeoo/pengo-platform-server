package com.ruoyi.agent.infrastructure.dify;

import com.ruoyi.agent.infrastructure.dify.model.DifyChatRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.io.IOException;
import java.util.function.Consumer;

/** Dify Chatflow 远程访问边界。 */
public interface DifyChatflowClient
{
    /** 发送流式消息，并逐个回调解析后的事件。 */
    void stream(DifyClientSettings settings, DifyChatRequest request, Consumer<DifyStreamEvent> consumer)
        throws IOException, InterruptedException;
    /** 停止指定 Dify task。 */
    void stop(DifyClientSettings settings, String taskId, String user) throws IOException, InterruptedException;
}
