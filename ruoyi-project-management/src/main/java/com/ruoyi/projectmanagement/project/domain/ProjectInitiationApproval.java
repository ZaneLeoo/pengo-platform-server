package com.ruoyi.projectmanagement.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 项目立项审批记录。 */
@Data
public class ProjectInitiationApproval {

    private Long approvalId;

    private Long workflowInstanceId;

    private Long projectId;

    /** 审批版本号。 */
    private Integer versionNo;

    /** 申请材料快照（JSON）。 */
    private String snapshotJson;

    private String submitBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime submitTime;

    /** PENDING、APPROVED、RETURNED。 */
    private String status;

    private String reviewBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime reviewTime;

    private String reviewComment;
}
