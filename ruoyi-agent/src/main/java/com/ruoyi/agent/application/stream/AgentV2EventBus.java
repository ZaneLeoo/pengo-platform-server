package com.ruoyi.agent.application.stream;

import com.ruoyi.agent.api.v2.AgentV2StreamEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/** 进程内实时事件总线；事件本身先持久化，因此断线后可补发。 */
@Component
public class AgentV2EventBus
{
    private final ConcurrentHashMap<Long, Set<Consumer<AgentV2StreamEvent>>> subscribers = new ConcurrentHashMap<>();

    /** 订阅指定运行的实时事件。 */
    public AutoCloseable subscribe(Long runId, Consumer<AgentV2StreamEvent> consumer)
    {
        subscribers.computeIfAbsent(runId, key -> ConcurrentHashMap.newKeySet()).add(consumer);
        return () -> unsubscribe(runId, consumer);
    }

    /** 发布给当前进程内的所有订阅者，单个订阅者异常不影响其他连接。 */
    public void publish(AgentV2StreamEvent event)
    {
        Set<Consumer<AgentV2StreamEvent>> consumers = subscribers.get(event.runId());
        if (consumers == null)
        {
            return;
        }
        for (Consumer<AgentV2StreamEvent> consumer : consumers)
        {
            try
            {
                consumer.accept(event);
            }
            catch (RuntimeException ignored)
            {
                unsubscribe(event.runId(), consumer);
            }
        }
    }

    private void unsubscribe(Long runId, Consumer<AgentV2StreamEvent> consumer)
    {
        Set<Consumer<AgentV2StreamEvent>> consumers = subscribers.get(runId);
        if (consumers == null)
        {
            return;
        }
        consumers.remove(consumer);
        if (consumers.isEmpty())
        {
            subscribers.remove(runId, consumers);
        }
    }
}
