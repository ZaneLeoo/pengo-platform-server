package com.ruoyi.projectmanagement.wbs.domain;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 创建工作包及其初始交付要求的请求体。
 */
@Data
public class ProjectWorkPackageCreateRequest {

    /** 工作包基础信息。 */
    @Valid
    @NotNull(message = "工作包信息不能为空")
    private ProjectWbsNode workPackage;

    /** 创建时一并定义的正式交付要求。 */
    @Valid
    private List<ProjectDeliverable> deliverables = new ArrayList<>();
}
