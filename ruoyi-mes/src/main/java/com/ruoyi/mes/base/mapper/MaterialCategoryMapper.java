package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.MaterialCategory;
import java.util.List;

/**
 * 物料分类数据访问接口。
 *
 * @author ruoyi
 */
public interface MaterialCategoryMapper {

    /**
     * 查询物料分类列表。
     *
     * @param category 物料分类
     * @return 物料分类集合
     */
    List<MaterialCategory> selectCategoryList(MaterialCategory category);

    /**
     * 根据分类ID查询物料分类。
     *
     * @param categoryId 分类ID
     * @return 物料分类
     */
    MaterialCategory selectCategoryById(Long categoryId);

    /**
     * 根据分类编码查询物料分类。
     *
     * @param categoryCode 分类编码
     * @return 物料分类
     */
    MaterialCategory selectCategoryByCode(String categoryCode);

    /**
     * 查询子分类数量。
     *
     * @param categoryId 分类ID
     * @return 子分类数量
     */
    int countChildrenById(Long categoryId);

    /**
     * 新增物料分类。
     *
     * @param category 物料分类
     * @return 结果
     */
    int insertCategory(MaterialCategory category);

    /**
     * 修改物料分类。
     *
     * @param category 物料分类
     * @return 结果
     */
    int updateCategory(MaterialCategory category);

    /**
     * 删除物料分类。
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    int deleteCategoryById(Long categoryId);
}
