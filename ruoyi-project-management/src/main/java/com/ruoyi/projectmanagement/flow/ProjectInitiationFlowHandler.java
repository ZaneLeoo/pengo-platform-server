package com.ruoyi.projectmanagement.flow;

import com.ruoyi.flow.engine.enums.FlowInstanceStatus;
import com.ruoyi.flow.handler.FlowFinishedEvent;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 项目管理立项审批的流程终态监听：流程通过/驳回/撤销时更新立项状态。
 */
@Component
public class ProjectInitiationFlowHandler {

    private final IProjectInfoService projectInfoService;

    public ProjectInitiationFlowHandler(IProjectInfoService projectInfoService) {
        this.projectInfoService = projectInfoService;
    }

    /** 监听立项审批流程终态。 */
    @EventListener
    public void onFlowFinished(FlowFinishedEvent event) {
        if (!"PROJECT_INITIATION".equals(event.getInstance().getBizType())) {
            return;
        }
        Long projectId = event.getInstance().getBizId();
        String operator = event.getOperator();
        String status = event.getInstance().getStatus();
        if (FlowInstanceStatus.APPROVED.matches(status)) {
            projectInfoService.approveInitiation(projectId, operator);
        } else if (FlowInstanceStatus.REJECTED.matches(status)) {
            projectInfoService.rejectInitiation(projectId, event.getComment(), operator);
        } else {
            projectInfoService.cancelInitiation(projectId, operator);
        }
    }
}
