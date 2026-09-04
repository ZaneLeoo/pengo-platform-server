package com.ruoyi.projectmanagement.budget.mapper;

import com.ruoyi.projectmanagement.budget.domain.ProjectActualCost;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCostAggregate;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 项目实际成本数据访问。 */
@Mapper
public interface ProjectActualCostMapper {
    List<ProjectActualCost> selectByProjectId(Long projectId);

    ProjectActualCost selectById(Long actualCostId);

    int insert(ProjectActualCost cost);

    int update(ProjectActualCost cost);

    int deleteById(Long actualCostId);

    ProjectActualCost selectBySourceLineId(
            @Param("sourceType") String sourceType, @Param("sourceLineId") Long sourceLineId);

    int reverseBySourceLineId(
            @Param("sourceType") String sourceType,
            @Param("sourceLineId") Long sourceLineId,
            @Param("operator") String operator,
            @Param("reason") String reason);

    int restoreBySourceLineId(
            @Param("sourceType") String sourceType,
            @Param("sourceLineId") Long sourceLineId,
            @Param("operator") String operator);

    long countByCategoryId(Long costCategoryId);

    BigDecimal totalByProjectId(Long projectId);

    BigDecimal categoryTotal(
            @Param("projectId") Long projectId, @Param("costCategoryId") Long costCategoryId);

    BigDecimal workPackageCategoryTotal(
            @Param("projectId") Long projectId,
            @Param("workPackageId") Long workPackageId,
            @Param("costCategoryId") Long costCategoryId);

    List<ProjectActualCostAggregate> categoryTotals(Long projectId);

    List<ProjectActualCostAggregate> workPackageTotals(Long projectId);
}
