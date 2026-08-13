package com.ruoyi.flow.handler;

import com.ruoyi.flow.engine.domain.FlowInstance;

/**
 * 流程终态事件：实例通过/驳回/撤销时由引擎发布，业务模块通过 @EventListener 监听处理。
 */
public class FlowFinishedEvent {

    /** 流程实例。 */
    private final FlowInstance instance;

    /** 是否最终通过。 */
    private final boolean approved;

    /** 驳回意见（通过/撤销时为空）。 */
    private final String comment;

    /** 触发操作者。 */
    private final String operator;

    public FlowFinishedEvent(FlowInstance instance, boolean approved, String comment, String operator) {
        this.instance = instance;
        this.approved = approved;
        this.comment = comment;
        this.operator = operator;
    }

    public FlowInstance getInstance() {
        return instance;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getComment() {
        return comment;
    }

    public String getOperator() {
        return operator;
    }
}
