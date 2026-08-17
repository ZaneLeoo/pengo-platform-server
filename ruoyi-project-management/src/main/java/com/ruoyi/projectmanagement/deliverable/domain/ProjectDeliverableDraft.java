package com.ruoyi.projectmanagement.deliverable.domain;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

/**
 * 创建工作包时一并定义的初始交付要求。
 * 所属项目与所属工作包在服务端创建完工作包后回填，因此此处不校验这两个字段。
 */
@Data
public class ProjectDeliverableDraft {

    /** 交付物名称。 */
    @NotBlank(message = "交付物名称不能为空")
    private String deliverableName;

    /** 交付物类型编码：DOCUMENT、DRAWING、BOM、PROCESS、REPORT、FORM、EXTERNAL_LINK、OTHER。 */
    @NotBlank(message = "交付物类型不能为空")
    private String deliverableType;

    /** 提交方式快照：FILE、LINK；BUSINESS_OBJECT预留。 */
    private String submissionMode;

    /** 允许文件扩展名快照，逗号分隔；LINK类型为空。 */
    private String allowedExtensions;

    /** 是否必交：0选交，1必交。 */
    private String requiredFlag;

    /** 是否需审批：0不需审批，1需审批。 */
    private String approvalRequired;

    /** 说明。 */
    private String description;

    /** 计划交付日期。 */
    private LocalDate plannedDate;
}
