package com.ruoyi.projectmanagement.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.Data;

/**
 * 项目立项申请支撑材料。
 *
 * <p>approvalId 为空表示当前草稿附件；提交后绑定到审批版本，历史版本不再修改。
 */
@Data
public class ProjectInitiationAttachment {

    private Long attachmentId;

    private Long projectId;

    private Long approvalId;

    private Integer versionNo;

    /** BASIC_SCHEME、RESOURCE_BUDGET、RISK_ASSESSMENT。 */
    @NotBlank(message = "附件所属页签不能为空")
    private String sectionCode;

    @NotBlank(message = "附件名称不能为空")
    private String fileName;

    @NotBlank(message = "附件地址不能为空")
    private String fileUrl;

    private Long fileSize;

    private String fileExt;

    private String mimeType;

    private String description;

    private String uploadBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;
}
