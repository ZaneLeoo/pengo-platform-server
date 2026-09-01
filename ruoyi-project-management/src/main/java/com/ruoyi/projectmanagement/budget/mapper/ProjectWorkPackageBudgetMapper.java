package com.ruoyi.projectmanagement.budget.mapper;

import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectWorkPackageBudgetMapper {
    List<ProjectWorkPackageBudgetLine> selectByProjectId(Long projectId);

    List<ProjectWorkPackageBudgetLine> selectByWorkPackageId(Long workPackageId);

    ProjectWorkPackageBudgetLine selectById(Long lineId);

    int insert(ProjectWorkPackageBudgetLine line);

    int update(ProjectWorkPackageBudgetLine line);

    int deleteById(Long lineId);

    int countByWorkPackageId(Long workPackageId);

    long countByCategoryId(Long costCategoryId);

    int countByWorkPackageAndCategory(
            @Param("workPackageId") Long workPackageId,
            @Param("costCategoryId") Long costCategoryId,
            @Param("excludeId") Long excludeId);
}
