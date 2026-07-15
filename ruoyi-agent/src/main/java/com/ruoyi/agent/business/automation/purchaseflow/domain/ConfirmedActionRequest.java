package com.ruoyi.agent.business.automation.purchaseflow.domain;

import lombok.Data;

/** 登录用户确认自动化动作时提交的请求。 */
@Data
public class ConfirmedActionRequest<T> {
    private String requestId;
    private T draft;
}
