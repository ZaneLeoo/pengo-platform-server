package com.ruoyi.projectmanagement.costcategory.mapper;

import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 成本类别数据访问。 */
@Mapper
public interface CostCategoryMapper {
    List<CostCategory> selectList();

    CostCategory selectById(Long id);

    CostCategory selectByCode(String code);

    int countSameName(
            @Param("parentId") Long parentId,
            @Param("categoryName") String categoryName,
            @Param("excludeId") Long excludeId);

    int countChildren(Long id);

    int insert(CostCategory category);

    int update(CostCategory category);

    int updateHierarchy(CostCategory category);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("operator") String operator);

    int deleteById(Long id);
}
