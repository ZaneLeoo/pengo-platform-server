package com.ruoyi.projectmanagement.person.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目人员档案。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPerson extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 人员档案ID。
     */
    private Long personId;

    /**
     * 工号，项目管理范围内唯一。
     */
    @Excel(name = "工号")
    private String personCode;

    /**
     * 人员姓名。
     */
    @Excel(name = "姓名")
    private String personName;

    /**
     * 所属部门ID，关联 sys_dept.dept_id。
     */
    private Long deptId;

    /**
     * 所属部门名称，仅用于查询展示。
     */
    @Excel(name = "部门")
    private String deptName;

    /**
     * 岗位或专业角色。
     */
    @Excel(name = "岗位")
    private String positionName;

    /**
     * 工作邮箱。
     */
    @Excel(name = "邮箱")
    private String email;

    /**
     * 联系电话。
     */
    @Excel(name = "联系电话")
    private String mobile;

    /**
     * 档案状态：0 启用，1 停用。
     */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    @NotBlank(message = "工号不能为空")
    @Size(max = 32, message = "工号长度不能超过32个字符")
    public String getPersonCode() {
        return personCode;
    }

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    public String getPersonName() {
        return personName;
    }

    @NotNull(message = "所属部门不能为空")
    public Long getDeptId() {
        return deptId;
    }

    @NotBlank(message = "岗位不能为空")
    @Size(max = 100, message = "岗位长度不能超过100个字符")
    public String getPositionName() {
        return positionName;
    }

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    public String getEmail() {
        return email;
    }
}
