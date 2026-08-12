package com.ruoyi.projectmanagement.team.domain;
import com.ruoyi.common.core.domain.BaseEntity; import jakarta.validation.constraints.NotBlank; import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) public class ProjectRole extends BaseEntity { private Long roleId; private Long projectId; @NotBlank private String roleCode; @NotBlank private String roleName; private String systemFlag; private String status; private Integer sortOrder; }
