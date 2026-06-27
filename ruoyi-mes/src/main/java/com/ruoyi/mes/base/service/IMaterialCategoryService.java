package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.MaterialCategory;
import java.util.List;

/**
 * 物料分类业务接口。
 *
 * @author ruoyi
 */
public interface IMaterialCategoryService {

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
     * 校验分类编码是否唯一。
     *
     * @param category 物料分类
     * @return true 唯一
     */
    boolean checkCategoryCodeUnique(MaterialCategory category);

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
