package com.ruoyi.projectmanagement.budget.mapper;

import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectBudgetMapper {
    List<ProjectBudgetLine> selectByProjectId(Long projectId);

    ProjectBudgetLine selectById(Long budgetLineId);

    int insert(ProjectBudgetLine line);

    int update(ProjectBudgetLine line);

    int deleteById(Long budgetLineId);

    int deleteByProjectId(Long projectId);

    long countByCategoryId(Long costCategoryId);

    int countByProjectAndCategory(
            @Param("projectId") Long projectId,
            @Param("costCategoryId") Long costCategoryId,
            @Param("excludeId") Long excludeId);
}
