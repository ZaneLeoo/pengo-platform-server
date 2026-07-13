package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.mapper.MaterialCategoryMapper;
import com.ruoyi.mes.base.mapper.MaterialMapper;
import com.ruoyi.mes.base.service.IMaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物料分类业务处理。
 *
 * @author ruoyi
 */
@Service
public class MaterialCategoryServiceImpl implements IMaterialCategoryService {

    @Autowired
    private MaterialCategoryMapper categoryMapper;

    @Autowired
    private MaterialMapper materialMapper;

    /**
     * 查询物料分类列表。
     *
     * @param category
     *            物料分类
     * @return 物料分类集合
     */
    @Override
    public List<MaterialCategory> selectCategoryList(MaterialCategory category) {
        return categoryMapper.selectCategoryList(category);
    }

    /** 查询 Agent 工具使用的物料分类列表。 */
    @Override
    public List<MaterialCategory> selectCategoryListForAgent(String keyword, Long parentId, String status) {
        return categoryMapper.selectCategoryListForAgent(keyword, parentId, status);
    }

    /**
     * 根据分类ID查询物料分类。
     *
     * @param categoryId
     *            分类ID
     * @return 物料分类
     */
    @Override
    public MaterialCategory selectCategoryById(Long categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    /**
     * 校验分类编码是否唯一。
     *
     * @param category
     *            物料分类
     * @return true 唯一
     */
    @Override
    public boolean checkCategoryCodeUnique(MaterialCategory category) {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        MaterialCategory info = categoryMapper.selectCategoryByCode(category.getCategoryCode());
        return StringUtils.isNull(info) || info.getCategoryId().longValue() == categoryId.longValue();
    }

    /**
     * 新增物料分类。
     *
     * @param category
     *            物料分类
     * @return 结果
     */
    @Override
    public int insertCategory(MaterialCategory category) {
        fillAncestors(category);
        return categoryMapper.insertCategory(category);
    }

    /**
     * 修改物料分类。
     *
     * @param category
     *            物料分类
     * @return 结果
     */
    @Override
    public int updateCategory(MaterialCategory category) {
        fillAncestors(category);
        return categoryMapper.updateCategory(category);
    }

    /**
     * 删除物料分类。
     *
     * @param categoryId
     *            分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long categoryId) {
        if (categoryMapper.countChildrenById(categoryId) > 0) {
            throw new ServiceException("存在下级物料分类，不能删除");
        }
        if (materialMapper.countMaterialByCategoryId(categoryId) > 0) {
            throw new ServiceException("分类下存在物料，不能删除");
        }
        return categoryMapper.deleteCategoryById(categoryId);
    }

    /**
     * 填充祖级路径。
     *
     * @param category
     *            物料分类
     */
    private void fillAncestors(MaterialCategory category) {
        if (StringUtils.isNull(category.getParentId()) || category.getParentId() == 0L) {
            category.setParentId(0L);
            category.setAncestors("0");
            return;
        }
        MaterialCategory parent = categoryMapper.selectCategoryById(category.getParentId());
        if (StringUtils.isNull(parent)) {
            throw new ServiceException("父级物料分类不存在");
        }
        if ("1".equals(parent.getStatus())) {
            throw new ServiceException("父级物料分类已停用");
        }
        category.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
    }
}
