package com.ruoyi.projectmanagement.change.domain;

import lombok.Data;

@Data
public class ProjectPlanChangeAttachment {
    private Long attachmentId;
    private Long changeId;
    private String fileName;
    private String fileUrl;
}
