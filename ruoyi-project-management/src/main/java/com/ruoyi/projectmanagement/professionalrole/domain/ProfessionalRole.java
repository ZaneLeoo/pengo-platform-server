package com.ruoyi.projectmanagement.professionalrole.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局项目专业角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProfessionalRole extends BaseEntity {

    private Long professionalRoleId;

    @NotBlank(message = "专业角色编码不能为空")
    @Size(max = 50, message = "专业角色编码长度不能超过50个字符")
    private String roleCode;

    @NotBlank(message = "专业角色名称不能为空")
    @Size(max = 100, message = "专业角色名称长度不能超过100个字符")
    private String roleName;

    @Size(max = 500, message = "专业角色说明长度不能超过500个字符")
    private String roleDescription;

    /** 1 系统预置，0 自定义。 */
    private String systemFlag;

    /** 0 启用，1 停用。 */
    private String status;

    private Integer sortOrder;
}
