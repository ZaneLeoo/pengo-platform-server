package com.ruoyi.projectmanagement.category.mapper;

import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** 项目分类数据访问接口。 */
@Mapper
public interface ProjectCategoryMapper {

    /** 查询项目分类列表。 */
    List<ProjectCategory> selectProjectCategoryList(ProjectCategory category);

    /** 根据ID查询项目分类。 */
    ProjectCategory selectProjectCategoryById(Long categoryId);

    /** 根据编码查询项目分类。 */
    ProjectCategory selectProjectCategoryByCode(String categoryCode);

    /** 统计分类子节点数量。 */
    int countChildren(Long categoryId);

    /** 新增项目分类。 */
    int insertProjectCategory(ProjectCategory category);

    /** 修改项目分类。 */
    int updateProjectCategory(ProjectCategory category);

    /** 删除项目分类。 */
    int deleteProjectCategoryById(Long categoryId);
}
