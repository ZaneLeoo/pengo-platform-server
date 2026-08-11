package com.ruoyi.projectmanagement.category.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.domain.ProjectCategory;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.category.service.IProjectCategoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 项目分类业务实现。 */
@Service
public class ProjectCategoryServiceImpl implements IProjectCategoryService {
    private final ProjectCategoryMapper categoryMapper;

    public ProjectCategoryServiceImpl(ProjectCategoryMapper categoryMapper) { this.categoryMapper = categoryMapper; }

    @Override
    public List<ProjectCategory> selectProjectCategoryList(ProjectCategory category) { return categoryMapper.selectProjectCategoryList(category); }

    @Override
    public List<ProjectCategory> selectProjectCategoryTree() {
        List<ProjectCategory> categories = categoryMapper.selectProjectCategoryList(new ProjectCategory());
        Map<Long, List<ProjectCategory>> childrenByParent = categories.stream().collect(Collectors.groupingBy(ProjectCategory::getParentId));
        categories.forEach(category -> category.setChildren(childrenByParent.getOrDefault(category.getCategoryId(), new ArrayList<>())));
        return childrenByParent.getOrDefault(0L, new ArrayList<>());
    }

    @Override
    public ProjectCategory selectProjectCategoryById(Long categoryId) { return categoryMapper.selectProjectCategoryById(categoryId); }

    @Override
    public boolean checkCategoryCodeUnique(ProjectCategory category) {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        ProjectCategory existing = categoryMapper.selectProjectCategoryByCode(category.getCategoryCode());
        return StringUtils.isNull(existing) || existing.getCategoryId().longValue() == categoryId.longValue();
    }

    @Override
    public int insertProjectCategory(ProjectCategory category) {
        ProjectCategory parent = getParent(category.getParentId());
        category.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + parent.getCategoryId());
        return categoryMapper.insertProjectCategory(category);
    }

    @Override
    public int updateProjectCategory(ProjectCategory category) {
        if (category.getCategoryId().equals(category.getParentId())) throw new ServiceException("分类不能选择自己作为上级");
        ProjectCategory parent = getParent(category.getParentId());
        category.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + parent.getCategoryId());
        return categoryMapper.updateProjectCategory(category);
    }

    @Override
    public int deleteProjectCategoryById(Long categoryId) {
        if (categoryMapper.countChildren(categoryId) > 0) throw new ServiceException("存在子分类，不能删除");
        return categoryMapper.deleteProjectCategoryById(categoryId);
    }

    private ProjectCategory getParent(Long parentId) {
        if (parentId == null || parentId == 0L) return null;
        ProjectCategory parent = categoryMapper.selectProjectCategoryById(parentId);
        if (parent == null) throw new ServiceException("上级分类不存在");
        return parent;
    }
}
