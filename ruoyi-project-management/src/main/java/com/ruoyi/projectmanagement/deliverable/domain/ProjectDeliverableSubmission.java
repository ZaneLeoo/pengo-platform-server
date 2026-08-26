package com.ruoyi.projectmanagement.deliverable.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/** 交付物的提交与审核留痕。 */
@Data
public class ProjectDeliverableSubmission {

    private Long submissionId;

    private Long workflowInstanceId;

    private Long deliverableId;

    private Integer versionNo;

    private String fileUrl;

    private String externalUrl;

    private String submitBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** SUBMITTED、APPROVED、RETURNED、DELIVERED。 */
    private String reviewResult;

    private String reviewComment;

    private String reviewBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
}
