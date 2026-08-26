package com.ruoyi.projectmanagement.issue.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目问题的评论、状态变化和附件动态。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectIssueActivity extends BaseEntity {

    /** 动态ID。 */
    private Long activityId;

    /** 问题ID。 */
    private Long issueId;

    /** 类型：CREATED、COMMENT、STATUS、ATTACHMENT。 */
    private String activityType;

    /** 评论、操作原因或说明。 */
    private String content;

    /** 变化前状态。 */
    private String fromStatus;

    /** 变化后状态。 */
    private String toStatus;

    /** 附件名称。 */
    private String attachmentName;

    /** 附件地址。 */
    private String attachmentUrl;

    /** 操作用户ID。 */
    private Long operatorUserId;

    /** 操作人姓名。 */
    private String operatorName;
}
