package com.ruoyi.agent.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 保存活跃会话对应的 Dify task，供停止生成使用。 */
@Component
public class AgentStreamRegistry
{
    private final Map<Long, String> tasks = new ConcurrentHashMap<>();
    private final Set<Long> stopRequests = ConcurrentHashMap.newKeySet();
    /** 记录活跃 task。 */
    public void register(Long conversationId, String taskId) { if (taskId != null) tasks.put(conversationId, taskId); }
    /** 查询活跃 task。 */
    public String find(Long conversationId) { return tasks.get(conversationId); }
    /** 标记用户主动停止。 */
    public void requestStop(Long conversationId) { stopRequests.add(conversationId); }
    /** 判断是否由用户主动停止。 */
    public boolean isStopRequested(Long conversationId) { return stopRequests.contains(conversationId); }
    /** 撤销停止标记。 */
    public void clearStopRequest(Long conversationId) { stopRequests.remove(conversationId); }
    /** 清理已结束 task 及其停止标记。 */
    public void remove(Long conversationId) { tasks.remove(conversationId); stopRequests.remove(conversationId); }
}
