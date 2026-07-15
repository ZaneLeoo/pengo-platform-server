package com.ruoyi.web.service.agent;

import com.ruoyi.agent.domain.enums.DifyEventType;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 处理 Dify Chatflow 生命周期事件。
 *
 * <p>生命周期事件仅用于后端观测和失败诊断，不直接改变现有前端事件协议。</p>
 */
@Component
public class DifyWorkflowEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DifyWorkflowEventHandler.class);

    /**
     * 记录 Chatflow 工作流和节点的执行状态。
     *
     * @param event Dify 原始流式事件
     * @return 是否为已处理的生命周期事件
     */
    public boolean handle(DifyStreamEvent event) {
        DifyEventType eventType = DifyEventType.fromCode(event.getEvent());
        if (eventType == DifyEventType.WORKFLOW_STARTED) {
            logWorkflowStarted(event);
            return true;
        }
        if (eventType == DifyEventType.NODE_STARTED || eventType == DifyEventType.NODE_FINISHED) {
            logNodeEvent(eventType, event);
            return true;
        }
        if (eventType == DifyEventType.WORKFLOW_FINISHED) {
            logWorkflowFinished(event);
            return true;
        }
        return false;
    }

    private void logWorkflowStarted(DifyStreamEvent event) {
        LOGGER.debug("Dify Chatflow started: workflowRunId={}, taskId={}",
                event.getWorkflowRunId(), event.getTaskId());
    }

    private void logNodeEvent(DifyEventType eventType, DifyStreamEvent event) {
        Map<String, Object> data = event.getData();
        LOGGER.debug("Dify Chatflow node event: event={}, workflowRunId={}, nodeId={}, title={}, status={}",
                eventType.getCode(), event.getWorkflowRunId(), data.get("node_id"), data.get("title"),
                data.get("status"));
    }

    private void logWorkflowFinished(DifyStreamEvent event) {
        Map<String, Object> data = event.getData();
        LOGGER.debug("Dify Chatflow finished: workflowRunId={}, taskId={}, status={}, error={}",
                event.getWorkflowRunId(), event.getTaskId(), data.get("status"), data.get("error"));
    }
}
