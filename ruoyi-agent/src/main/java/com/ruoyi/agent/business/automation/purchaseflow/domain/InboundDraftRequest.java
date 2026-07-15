package com.ruoyi.agent.business.automation.purchaseflow.domain;

import java.util.List;
import lombok.Data;

/** AI 准备采购入库草稿的请求。 */
@Data
public class InboundDraftRequest {
    private String inboundDate;
    private String remark;
    private List<InboundDraftLineRequest> lines;
}
