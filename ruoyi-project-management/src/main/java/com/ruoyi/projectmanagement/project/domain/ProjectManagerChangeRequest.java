package com.ruoyi.projectmanagement.project.domain;

import jakarta.validation.constraints.NotNull;

/**
 * 项目负责人变更请求。
 */
public class ProjectManagerChangeRequest {

    /** 新负责人系统用户ID。 */
    @NotNull(message = "新项目负责人不能为空")
    private Long managerId;

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
}
