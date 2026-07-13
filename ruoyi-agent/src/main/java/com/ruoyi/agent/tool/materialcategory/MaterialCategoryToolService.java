package com.ruoyi.agent.tool.materialcategory;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.service.IMaterialCategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 物料分类查询工具应用服务。 */
@Service
public class MaterialCategoryToolService
{
    private final IMaterialCategoryService categoryService;

    public MaterialCategoryToolService(IMaterialCategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    /** 查询分类并转换成适合模型消费的层级信息。 */
    public AgentToolResult<List<MaterialCategoryToolItem>> query(MaterialCategoryToolQuery request)
    {
        MaterialCategoryToolQuery query = request == null ? new MaterialCategoryToolQuery() : request;
        String status = Boolean.FALSE.equals(query.includeDisabled()) ? "0" : null;
        List<MaterialCategoryToolItem> data = categoryService.selectCategoryListForAgent(query.keyword(),
            query.parentId(), status).stream().map(this::toItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(data.size(), false);
        if (data.isEmpty())
        {
            return AgentToolResults.noResult(MaterialCategoryToolResultCode.MATERIAL_CATEGORY_NOT_FOUND,
                "当前条件没有查询到物料分类", data, meta);
        }
        return AgentToolResults.success(MaterialCategoryToolResultCode.MATERIAL_CATEGORY_QUERY_SUCCESS,
            "查询到 " + data.size() + " 条物料分类", data, meta);
    }

    /** 将分类实体转换为工具输出 DTO。 */
    private MaterialCategoryToolItem toItem(MaterialCategory category)
    {
        String ancestors = category.getAncestors();
        int level = ancestors == null || ancestors.isBlank() ? 1 : ancestors.split(",").length;
        return new MaterialCategoryToolItem(category.getCategoryId(), category.getCategoryCode(),
            category.getCategoryName(), category.getParentId(), ancestors, level, category.getStatus());
    }
}
