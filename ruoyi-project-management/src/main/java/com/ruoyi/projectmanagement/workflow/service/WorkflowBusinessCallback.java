package com.ruoyi.projectmanagement.workflow.service;

/** 审批结束后回写业务状态的扩展点。 */
public interface WorkflowBusinessCallback {
    /** 对应业务类型。 */
    String businessType();

    /** 全部节点同意后的业务回写。 */
    void approved(Long businessId, String operator, String opinion);

    /** 任一节点驳回后的业务回写。 */
    void rejected(Long businessId, String operator, String opinion);
}
