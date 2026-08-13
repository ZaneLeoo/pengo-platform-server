package com.ruoyi.flow.engine.domain;

import lombok.Data;

/**
 * 审批流转历史：一条实例的完整审批链条记录。
 */
@Data
public class FlowHistory {

    /** 历史记录ID。 */
    private Long historyId;

    /** 流程实例ID。 */
    private Long instanceId;

    /** 节点ID，提交记录为空。 */
    private Long nodeId;

    /** 节点名称。 */
    private String nodeName;

    /** 动作：SUBMIT提交，APPROVE同意，REJECT驳回，CANCEL撤销。 */
    private String action;

    /** 操作人登录名。 */
    private String operator;

    /** 操作人姓名。 */
    private String operatorName;

    /** 审批意见。 */
    private String comment;

    /** 操作时间。 */
    private java.util.Date operateTime;
}
