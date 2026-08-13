package com.ruoyi.projectmanagement.flow;

import com.ruoyi.flow.engine.domain.FlowInstance;
import com.ruoyi.flow.handler.FlowBizHandler;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import org.springframework.stereotype.Component;

/**
 * 项目管理立项审批的流程回调：流程通过/驳回/撤销时更新立项状态。
 */
@Component
public class ProjectInitiationFlowHandler implements FlowBizHandler {

    private final IProjectInfoService projectInfoService;

    public ProjectInitiationFlowHandler(IProjectInfoService projectInfoService) {
        this.projectInfoService = projectInfoService;
    }

    /** 业务类型：项目管理立项申请。 */
    @Override
    public String bizType() {
        return "PROJECT_INITIATION";
    }

    /** 流程通过：立项通过并生成正式WBS。 */
    @Override
    public void onApproved(FlowInstance instance, String operator) {
        projectInfoService.approveInitiation(instance.getBizId(), operator);
    }

    /** 流程驳回：立项退回草稿。 */
    @Override
    public void onRejected(FlowInstance instance, String comment, String operator) {
        projectInfoService.rejectInitiation(instance.getBizId(), comment, operator);
    }

    /** 流程撤销：立项退回草稿。 */
    @Override
    public void onCancelled(FlowInstance instance, String operator) {
        projectInfoService.cancelInitiation(instance.getBizId(), operator);
    }
}
