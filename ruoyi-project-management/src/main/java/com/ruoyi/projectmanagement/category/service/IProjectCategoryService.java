package com.ruoyi.projectmanagement.category.service;

import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import java.util.List;

/** 项目分类业务接口。 */
public interface IProjectCategoryService {

    /** 查询项目分类列表。 */
    List<ProjectCategory> selectProjectCategoryList(ProjectCategory category);

    /** 查询项目分类树。 */
    List<ProjectCategory> selectProjectCategoryTree();

    /** 根据ID查询项目分类。 */
    ProjectCategory selectProjectCategoryById(Long categoryId);

    /** 校验分类编码是否唯一。 */
    boolean checkCategoryCodeUnique(ProjectCategory category);

    /** 新增项目分类。 */
    int insertProjectCategory(ProjectCategory category);

    /** 修改项目分类。 */
    int updateProjectCategory(ProjectCategory category);

    /** 删除项目分类。 */
    int deleteProjectCategoryById(Long categoryId);
}
