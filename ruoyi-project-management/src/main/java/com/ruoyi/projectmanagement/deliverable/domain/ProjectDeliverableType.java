package com.ruoyi.projectmanagement.deliverable.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目正式交付物类型配置。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDeliverableType extends BaseEntity {

    private Long typeId;

    @NotBlank(message = "类型编码不能为空")
    private String typeCode;

    @NotBlank(message = "类型名称不能为空")
    private String typeName;

    /** FILE文件、LINK外链、BUSINESS_OBJECT业务对象。 */
    @NotBlank(message = "提交方式不能为空")
    private String submissionMode;

    /** 默认是否需要审批：0否、1是。 */
    private String defaultApprovalRequired;

    /** 状态：0启用、1停用。 */
    private String status;

    private Integer sortOrder;

    /** 当前类型允许的文件扩展名；LINK类型为空。 */
    private List<String> allowedExtensions = new ArrayList<>();
}
