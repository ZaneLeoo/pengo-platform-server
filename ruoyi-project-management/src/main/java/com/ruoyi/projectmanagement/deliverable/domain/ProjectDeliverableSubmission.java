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

    /** 业务对象类型；BOM 交付固定为 BOM_VERSION。 */
    private String businessType;

    /** 业务对象ID；BOM 交付关联 bom_version.id。 */
    private String businessId;

    /** 提交时的业务对象编码快照。 */
    private String businessCode;

    /** 提交时的业务对象名称快照。 */
    private String businessName;

    /** 提交时的业务对象版本快照。 */
    private String businessVersion;

    private String submitBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date submitTime;

    /** SUBMITTED、APPROVED、RETURNED、DELIVERED。 */
    private String reviewResult;

    private String reviewComment;

    private String reviewBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date reviewTime;
}
