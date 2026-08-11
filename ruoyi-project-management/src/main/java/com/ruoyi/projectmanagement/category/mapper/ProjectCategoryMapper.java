package com.ruoyi.projectmanagement.category.mapper;

import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** 项目分类数据访问。 */
@Mapper
public interface ProjectCategoryMapper {
    List<ProjectCategory> selectProjectCategoryList(ProjectCategory category);
    ProjectCategory selectProjectCategoryById(Long categoryId);
    ProjectCategory selectProjectCategoryByCode(String categoryCode);
    int countChildren(Long categoryId);
    int insertProjectCategory(ProjectCategory category);
    int updateProjectCategory(ProjectCategory category);
    int deleteProjectCategoryById(Long categoryId);
}
