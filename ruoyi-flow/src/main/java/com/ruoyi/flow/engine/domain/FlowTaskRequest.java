package com.ruoyi.flow.engine.domain;

import lombok.Data;

/**
 * 审批处理请求（同意/驳回）。
 */
@Data
public class FlowTaskRequest {

    /** 审批意见；驳回时必填。 */
    private String comment;
}
