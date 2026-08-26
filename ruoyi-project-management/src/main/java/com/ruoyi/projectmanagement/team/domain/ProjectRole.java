package com.ruoyi.projectmanagement.team.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目角色实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectRole extends BaseEntity {

    private Long roleId;

    /** 所属项目ID，0 表示系统预置角色。 */
    private Long projectId;

    @NotBlank private String roleCode;

    @NotBlank private String roleName;

    /** 是否系统预置：1 是，0 否。 */
    private String systemFlag;

    /** 状态：0 启用，1 停用。 */
    private String status;

    private Integer sortOrder;
}
