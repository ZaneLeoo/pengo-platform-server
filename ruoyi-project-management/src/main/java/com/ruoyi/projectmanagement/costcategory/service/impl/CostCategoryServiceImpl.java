package com.ruoyi.projectmanagement.costcategory.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategoryUsage;
import com.ruoyi.projectmanagement.costcategory.mapper.CostCategoryMapper;
import com.ruoyi.projectmanagement.costcategory.service.ICostCategoryService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 成本类别服务实现。 */
@Service
public class CostCategoryServiceImpl implements ICostCategoryService {

    private static final int MAX_LEVEL = 3;
    private final CostCategoryMapper mapper;
    private final ProjectBudgetMapper budgetMapper;
    private final ProjectWorkPackageBudgetMapper workPackageBudgetMapper;

    public CostCategoryServiceImpl(
            CostCategoryMapper mapper,
            ProjectBudgetMapper budgetMapper,
            ProjectWorkPackageBudgetMapper workPackageBudgetMapper) {
        this.mapper = mapper;
        this.budgetMapper = budgetMapper;
        this.workPackageBudgetMapper = workPackageBudgetMapper;
    }

    @Override
    public List<CostCategory> tree(CostCategory filter) {
        List<CostCategory> all = enrich(mapper.selectList());
        if (filter == null || noFilter(filter)) {
            return roots(all);
        }
        Predicate<CostCategory> matches =
                item ->
                        contains(item.getCategoryCode(), filter.getCategoryCode())
                                && contains(item.getCategoryName(), filter.getCategoryName())
                                && (blank(filter.getStatus())
                                        || filter.getStatus().equals(item.getStatus()));
        return prune(roots(all), matches);
    }

    @Override
    public List<CostCategory> options() {
        return enrich(mapper.selectList()).stream()
                .filter(item -> Boolean.TRUE.equals(item.getLeaf()))
                .filter(item -> "0".equals(item.getEffectiveStatus()))
                .map(this::withoutChildren)
                .toList();
    }

    @Override
    public CostCategory get(Long id) {
        CostCategory category = mapper.selectById(id);
        if (category == null) {
            throw new ServiceException("成本类别不存在");
        }
        return category;
    }

    @Override
    public CostCategoryUsage usage(Long id) {
        CostCategory category = get(id);
        long references = referenceCount(id);
        boolean hasChildren = mapper.countChildren(id) > 0;
        boolean system = "1".equals(category.getSystemFlag());
        String reason = null;
        if (system) reason = "系统预置类别不可删除或移动";
        else if (hasChildren) reason = "存在下级类别";
        else if (references > 0) reason = "类别已被业务使用";
        return new CostCategoryUsage(
                references,
                !system && !hasChildren && references == 0,
                !system && references == 0,
                category.getLevelNo() < MAX_LEVEL && references == 0,
                reason);
    }

    @Override
    @Transactional
    public int add(CostCategory category, String operator) {
        normalize(category);
        if (mapper.selectByCode(category.getCategoryCode()) != null) {
            throw new ServiceException("成本类别编码已存在");
        }
        CostCategory parent = parent(category.getParentId());
        if (parent != null && parent.getLevelNo() >= MAX_LEVEL) {
            throw new ServiceException("成本类别最多支持三级");
        }
        if (parent != null && referenceCount(parent.getCostCategoryId()) > 0) {
            throw new ServiceException("上级类别已被业务使用，不能再新增下级");
        }
        validateSameName(category.getParentId(), category.getCategoryName(), null);
        setHierarchy(category, parent);
        category.setSystemFlag("0");
        category.setCreateBy(operator);
        return mapper.insert(category);
    }

    @Override
    @Transactional
    public int edit(CostCategory category, String operator) {
        CostCategory old = get(category.getCostCategoryId());
        normalize(category);
        if (!old.getCategoryCode().equals(category.getCategoryCode())) {
            throw new ServiceException("成本类别编码保存后不能修改");
        }
        Long requestedParent = normalizeParent(category.getParentId());
        boolean moving = !normalizeParent(old.getParentId()).equals(requestedParent);
        if ("1".equals(old.getSystemFlag()) && moving) {
            throw new ServiceException("系统预置类别不能修改上级");
        }
        if (moving && referenceCount(old.getCostCategoryId()) > 0) {
            throw new ServiceException("成本类别已被业务使用，不能修改上级");
        }
        CostCategory parent = parent(requestedParent);
        validateMove(old, parent);
        validateSameName(requestedParent, category.getCategoryName(), old.getCostCategoryId());
        setHierarchy(category, parent);
        category.setSystemFlag(old.getSystemFlag());
        category.setUpdateBy(operator);
        int rows = mapper.update(category);
        if (moving) {
            updateDescendantHierarchy(old, category);
        }
        return rows;
    }

    @Override
    @Transactional
    public int changeStatus(Long id, String status, String operator) {
        get(id);
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("成本类别状态不正确");
        }
        return mapper.updateStatus(id, status, operator);
    }

    @Override
    @Transactional
    public int remove(Long id) {
        CostCategoryUsage usage = usage(id);
        if (!Boolean.TRUE.equals(usage.getCanDelete())) {
            throw new ServiceException(usage.getReadonlyReason());
        }
        return mapper.deleteById(id);
    }

    private List<CostCategory> enrich(List<CostCategory> all) {
        Map<Long, CostCategory> byId =
                all.stream()
                        .collect(Collectors.toMap(CostCategory::getCostCategoryId, item -> item));
        Map<Long, List<CostCategory>> children =
                all.stream()
                        .collect(
                                Collectors.groupingBy(item -> normalizeParent(item.getParentId())));
        for (CostCategory item : all) {
            List<CostCategory> direct =
                    children.getOrDefault(item.getCostCategoryId(), new ArrayList<>());
            item.setChildren(direct);
            item.setLeaf(direct.isEmpty());
            item.setFullPath(path(item, byId));
            item.setEffectiveStatus(effectiveStatus(item, byId));
            CostCategoryUsage usage = usageFor(item, direct.isEmpty());
            item.setUsageCount(usage.getUsageCount());
            item.setCanEdit(true);
            item.setCanDelete(usage.getCanDelete());
            item.setCanAddChild(usage.getCanAddChild());
            item.setReadonlyReason(usage.getReadonlyReason());
        }
        return all;
    }

    private CostCategoryUsage usageFor(CostCategory item, boolean leaf) {
        long references = referenceCount(item.getCostCategoryId());
        boolean system = "1".equals(item.getSystemFlag());
        String reason = null;
        if (system) reason = "系统预置类别不可删除或移动";
        else if (!leaf) reason = "存在下级类别";
        else if (references > 0) reason = "类别已被业务使用";
        return new CostCategoryUsage(
                references,
                !system && leaf && references == 0,
                !system && references == 0,
                item.getLevelNo() < MAX_LEVEL && references == 0,
                reason);
    }

    private void validateMove(CostCategory current, CostCategory parent) {
        if (parent != null) {
            if (parent.getCostCategoryId().equals(current.getCostCategoryId())
                    || ancestorIds(parent).contains(current.getCostCategoryId())) {
                throw new ServiceException("成本类别不能移动到自身或下级");
            }
        }
        int newLevel = parent == null ? 1 : parent.getLevelNo() + 1;
        int descendantDepth =
                mapper.selectList().stream()
                        .filter(item -> ancestorIds(item).contains(current.getCostCategoryId()))
                        .mapToInt(item -> item.getLevelNo() - current.getLevelNo())
                        .max()
                        .orElse(0);
        if (newLevel + descendantDepth > MAX_LEVEL) {
            throw new ServiceException("移动后成本类别将超过三级");
        }
    }

    private void updateDescendantHierarchy(CostCategory old, CostCategory updated) {
        String oldPrefix = old.getAncestors() + "," + old.getCostCategoryId();
        String newPrefix = updated.getAncestors() + "," + updated.getCostCategoryId();
        for (CostCategory child : mapper.selectList()) {
            if (ancestorIds(child).contains(old.getCostCategoryId())) {
                child.setAncestors(child.getAncestors().replaceFirst(oldPrefix, newPrefix));
                child.setLevelNo(child.getLevelNo() - old.getLevelNo() + updated.getLevelNo());
                child.setUpdateBy(updated.getUpdateBy());
                mapper.updateHierarchy(child);
            }
        }
    }

    private void validateSameName(Long parentId, String name, Long excludeId) {
        if (mapper.countSameName(normalizeParent(parentId), name, excludeId) > 0) {
            throw new ServiceException("同一上级下成本类别名称已存在");
        }
    }

    private CostCategory parent(Long parentId) {
        Long normalized = normalizeParent(parentId);
        if (normalized == 0L) return null;
        CostCategory parent = mapper.selectById(normalized);
        if (parent == null) throw new ServiceException("上级成本类别不存在");
        return parent;
    }

    private void setHierarchy(CostCategory item, CostCategory parent) {
        item.setParentId(parent == null ? 0L : parent.getCostCategoryId());
        item.setAncestors(
                parent == null ? "0" : parent.getAncestors() + "," + parent.getCostCategoryId());
        item.setLevelNo(parent == null ? 1 : parent.getLevelNo() + 1);
    }

    private void normalize(CostCategory item) {
        item.setParentId(normalizeParent(item.getParentId()));
        item.setCategoryCode(item.getCategoryCode().trim().toUpperCase(Locale.ROOT));
        item.setCategoryName(item.getCategoryName().trim());
        item.setAllowManualEntry("1".equals(item.getAllowManualEntry()) ? "1" : "0");
        item.setStatus("1".equals(item.getStatus()) ? "1" : "0");
        item.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
        item.setFinanceAccountCode(trimToNull(item.getFinanceAccountCode()));
        item.setFinanceAccountName(trimToNull(item.getFinanceAccountName()));
        item.setDescription(trimToNull(item.getDescription()));
    }

    private List<CostCategory> roots(List<CostCategory> all) {
        return all.stream().filter(item -> normalizeParent(item.getParentId()) == 0L).toList();
    }

    private List<CostCategory> prune(List<CostCategory> nodes, Predicate<CostCategory> matches) {
        List<CostCategory> result = new ArrayList<>();
        for (CostCategory node : nodes) {
            List<CostCategory> keptChildren = prune(node.getChildren(), matches);
            if (matches.test(node) || !keptChildren.isEmpty()) {
                node.setChildren(keptChildren);
                result.add(node);
            }
        }
        return result;
    }

    private String path(CostCategory item, Map<Long, CostCategory> byId) {
        List<String> names = new ArrayList<>();
        CostCategory cursor = item;
        Set<Long> visited = new HashSet<>();
        while (cursor != null && visited.add(cursor.getCostCategoryId())) {
            names.add(0, cursor.getCategoryName());
            cursor = byId.get(normalizeParent(cursor.getParentId()));
        }
        return String.join(" / ", names);
    }

    private String effectiveStatus(CostCategory item, Map<Long, CostCategory> byId) {
        CostCategory cursor = item;
        Set<Long> visited = new HashSet<>();
        while (cursor != null && visited.add(cursor.getCostCategoryId())) {
            if (!"0".equals(cursor.getStatus())) return "1";
            cursor = byId.get(normalizeParent(cursor.getParentId()));
        }
        return "0";
    }

    private Set<Long> ancestorIds(CostCategory item) {
        Set<Long> ids = new HashSet<>();
        if (item.getAncestors() == null) return ids;
        for (String value : item.getAncestors().split(",")) {
            try {
                long id = Long.parseLong(value);
                if (id > 0) ids.add(id);
            } catch (NumberFormatException ignored) {
                // Ignore malformed historical fragments and let hierarchy validation repair them.
            }
        }
        return ids;
    }

    private CostCategory withoutChildren(CostCategory source) {
        source.setChildren(new ArrayList<>());
        return source;
    }

    private long referenceCount(Long id) {
        return budgetMapper.countByCategoryId(id) + workPackageBudgetMapper.countByCategoryId(id);
    }

    private boolean noFilter(CostCategory filter) {
        return blank(filter.getCategoryCode())
                && blank(filter.getCategoryName())
                && blank(filter.getStatus());
    }

    private boolean contains(String source, String query) {
        return blank(query)
                || (source != null
                        && source.toLowerCase(Locale.ROOT)
                                .contains(query.trim().toLowerCase(Locale.ROOT)));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        return blank(value) ? null : value.trim();
    }

    private Long normalizeParent(Long id) {
        return id == null ? 0L : id;
    }
}
