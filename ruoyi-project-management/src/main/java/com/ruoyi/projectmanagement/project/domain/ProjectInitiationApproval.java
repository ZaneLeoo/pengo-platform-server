package com.ruoyi.projectmanagement.project.domain;
import java.time.LocalDateTime;import lombok.Data;
@Data public class ProjectInitiationApproval {private Long approvalId;private Long projectId;private Integer versionNo;private String snapshotJson;private String submitBy;private LocalDateTime submitTime;private String status;private String reviewBy;private LocalDateTime reviewTime;private String reviewComment;}
