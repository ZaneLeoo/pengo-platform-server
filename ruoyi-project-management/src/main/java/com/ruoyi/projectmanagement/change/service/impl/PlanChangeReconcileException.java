package com.ruoyi.projectmanagement.change.service.impl;

/** 基线冲突需要提交状态 RECONCILE 后再向调用方返回业务错误。 */
public class PlanChangeReconcileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PlanChangeReconcileException(String message) {
        super(message);
    }
}
