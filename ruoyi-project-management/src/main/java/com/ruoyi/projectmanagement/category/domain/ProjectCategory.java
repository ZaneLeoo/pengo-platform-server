package com.ruoyi.projectmanagement.category.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 项目分类。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /** 分类ID。 */ private Long categoryId;
    /** 上级分类ID，0 表示根节点。 */ private Long parentId;
    /** 祖级路径，逗号分隔。 */ private String ancestors;
    /** 分类编码。 */ private String categoryCode;
    /** 分类名称。 */ private String categoryName;
    /** 同级显示顺序。 */ private Integer orderNum;
    /** 状态：0 启用，1 停用。 */ private String status;
    /** 子分类，仅用于树形展示。 */ private List<ProjectCategory> children = new ArrayList<>();

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 32, message = "分类编码长度不能超过32个字符")
    public String getCategoryCode() { return categoryCode; }

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    public String getCategoryName() { return categoryName; }
}
