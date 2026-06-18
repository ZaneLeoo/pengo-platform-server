package com.ruoyi.agent.service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import com.ruoyi.agent.dto.DifyChatRequest;
import com.ruoyi.agent.dto.DifyEvent;

/**
 * Dify API 调用服务
 *
 * @author ruoyi
 */
public interface IDifyApiService
{
    /**
     * 流式调用Dify，通过Consumer回调每个SSE事件
     *
     * @param apiBaseUrl    Dify API基础地址
     * @param apiKey        Dify App API Key
     * @param request       Dify请求体
     * @param cancelled     取消标志（外部设置为true时中断读取）
     * @param eventConsumer 事件回调
     */
    void streamChat(String apiBaseUrl, String apiKey, DifyChatRequest request,
                    AtomicBoolean cancelled, Consumer<DifyEvent> eventConsumer) throws IOException;
}
