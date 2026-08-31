package com.ruoyi.projectmanagement.costcategory.service;

import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategoryUsage;
import java.util.List;

/** 成本类别服务。 */
public interface ICostCategoryService {
    List<CostCategory> tree(CostCategory filter);

    List<CostCategory> options();

    CostCategory get(Long id);

    CostCategoryUsage usage(Long id);

    int add(CostCategory category, String operator);

    int edit(CostCategory category, String operator);

    int changeStatus(Long id, String status, String operator);

    int remove(Long id);
}
