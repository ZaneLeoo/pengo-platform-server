package com.ruoyi.agent.business.automation.purchaseflow.domain;

import lombok.Data;

/** 登录用户确认审核采购单据的请求。 */
@Data
public class DocumentActionRequest {
    private String requestId;
    private Long documentId;
}
