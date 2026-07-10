package com.ruoyi.web.service.agent;

import com.ruoyi.agent.api.v2.AgentV2StreamEvent;
import com.ruoyi.agent.application.run.AgentRunService;
import com.ruoyi.agent.application.stream.AgentV2EventBus;
import com.ruoyi.agent.application.stream.AgentV2EventService;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 将可恢复的 Agent V2 事件日志适配为浏览器 SSE 连接。 */
@Service
public class AgentV2SseService
{
    private static final long SSE_TIMEOUT = Duration.ofMinutes(30).toMillis();

    private final AgentRunService runService;
    private final AgentV2EventService eventService;
    private final AgentV2EventBus eventBus;

    public AgentV2SseService(AgentRunService runService, AgentV2EventService eventService,
        AgentV2EventBus eventBus)
    {
        this.runService = runService;
        this.eventService = eventService;
        this.eventBus = eventBus;
    }

    /** 先订阅实时事件再补发历史，确保查询历史期间产生的事件不会丢失。 */
    public SseEmitter connect(Long runId, Long userId, Long afterSequence)
    {
        runService.requireOwned(runId, userId);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        StreamConnection connection = new StreamConnection(emitter, afterSequence == null ? 0L : afterSequence);
        emitter.onCompletion(connection::close);
        emitter.onTimeout(connection::timeout);
        emitter.onError(error -> connection.close());

        synchronized (connection.lock)
        {
            connection.setSubscription(eventBus.subscribe(runId, connection::send));
            for (AgentV2StreamEvent event : eventService.listAfter(runId, connection.lastSequence.get()))
            {
                connection.sendLocked(event);
            }
        }
        return emitter;
    }

    private static class StreamConnection
    {
        private final Object lock = new Object();
        private final SseEmitter emitter;
        private final AtomicLong lastSequence;
        private AutoCloseable subscription;
        private boolean closed;

        private StreamConnection(SseEmitter emitter, long afterSequence)
        {
            this.emitter = emitter;
            this.lastSequence = new AtomicLong(Math.max(0L, afterSequence));
        }

        private void setSubscription(AutoCloseable subscription)
        {
            this.subscription = subscription;
        }

        private void send(AgentV2StreamEvent event)
        {
            synchronized (lock)
            {
                sendLocked(event);
            }
        }

        private void sendLocked(AgentV2StreamEvent event)
        {
            if (closed || event.sequence() <= lastSequence.get())
            {
                return;
            }
            try
            {
                emitter.send(SseEmitter.event()
                    .id(String.valueOf(event.sequence()))
                    .name(event.event())
                    .data(event));
                lastSequence.set(event.sequence());
                if (isTerminal(event.event()))
                {
                    closed = true;
                    emitter.complete();
                    closeSubscription();
                }
            }
            catch (IOException | IllegalStateException error)
            {
                closed = true;
                closeSubscription();
                emitter.completeWithError(error);
            }
        }

        private void timeout()
        {
            synchronized (lock)
            {
                close();
                emitter.complete();
            }
        }

        private void close()
        {
            synchronized (lock)
            {
                closed = true;
                closeSubscription();
            }
        }

        private void closeSubscription()
        {
            if (subscription == null)
            {
                return;
            }
            try
            {
                subscription.close();
            }
            catch (Exception ignored)
            {
                // 关闭订阅是幂等清理动作，不影响已发送给客户端的结果。
            }
            subscription = null;
        }

        private boolean isTerminal(String event)
        {
            return "run.completed".equals(event) || "run.failed".equals(event) || "run.cancelled".equals(event);
        }
    }
}
