package com.ruoyi.flow.handler;

import com.ruoyi.flow.engine.domain.FlowInstance;

/**
 * 流程业务回调：流程实例到达终态（通过/驳回/撤销）时通知对应业务模块。
 * 由各业务模块实现并通过 Spring 注册，按 {@link #bizType()} 匹配。
 */
public interface FlowBizHandler {

    /** 支持的业务类型编码。 */
    String bizType();

    /** 流程全部节点通过后回调。 */
    void onApproved(FlowInstance instance, String operator);

    /** 流程被驳回后回调。 */
    void onRejected(FlowInstance instance, String comment, String operator);

    /** 流程被发起人撤销后回调。 */
    default void onCancelled(FlowInstance instance, String operator) {
        // 默认不做处理
    }
}
