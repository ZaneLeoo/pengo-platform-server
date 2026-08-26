package com.ruoyi.projectmanagement.task.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 执行任务的过程成果（附件或工作证据），不参与正式交付物审批。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectTaskOutput extends BaseEntity {

    /** 任务成果ID。 */
    private Long outputId;

    /** 所属执行任务ID。 */
    @NotNull(message = "所属任务不能为空")
    private Long taskId;

    /** 成果名称。 */
    @NotBlank(message = "成果名称不能为空")
    private String outputName;

    /** 成果文件地址。 */
    @NotBlank(message = "请上传成果文件")
    private String fileUrl;

    /** 成果说明。 */
    private String description;
}
