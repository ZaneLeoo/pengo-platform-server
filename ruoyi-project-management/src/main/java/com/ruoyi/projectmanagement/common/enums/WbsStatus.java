package com.ruoyi.projectmanagement.common.enums;

/** WBS与工作包汇总状态。 */
public enum WbsStatus {
    NOT_STARTED, ACTIVE, WAITING_DELIVERY, COMPLETED;
    public boolean matches(String value) { return name().equals(value); }
}
