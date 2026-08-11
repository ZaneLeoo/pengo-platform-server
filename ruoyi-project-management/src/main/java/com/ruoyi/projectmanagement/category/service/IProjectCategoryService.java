package com.ruoyi.projectmanagement.category.service;

import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import java.util.List;

/** 项目分类业务接口。 */
public interface IProjectCategoryService {
    List<ProjectCategory> selectProjectCategoryList(ProjectCategory category);
    List<ProjectCategory> selectProjectCategoryTree();
    ProjectCategory selectProjectCategoryById(Long categoryId);
    boolean checkCategoryCodeUnique(ProjectCategory category);
    int insertProjectCategory(ProjectCategory category);
    int updateProjectCategory(ProjectCategory category);
    int deleteProjectCategoryById(Long categoryId);
}
