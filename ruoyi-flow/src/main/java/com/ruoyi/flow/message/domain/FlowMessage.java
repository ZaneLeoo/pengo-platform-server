package com.ruoyi.flow.message.domain;

import lombok.Data;

/**
 * 流程站内消息：新待办产生时发送给审批人。
 */
@Data
public class FlowMessage {

    /** 消息ID。 */
    private Long messageId;

    /** 接收人登录名。 */
    private String receiver;

    /** 消息标题。 */
    private String title;

    /** 消息内容。 */
    private String content;

    /** 业务类型。 */
    private String bizType;

    /** 业务记录ID。 */
    private Long bizId;

    /** 流程实例ID。 */
    private Long instanceId;

    /** 已读：0未读，1已读。 */
    private String readFlag;

    /** 创建时间。 */
    private java.util.Date createTime;
}
