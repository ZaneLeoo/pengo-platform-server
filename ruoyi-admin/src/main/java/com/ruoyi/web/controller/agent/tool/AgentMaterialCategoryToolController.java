package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.materialcategory.MaterialCategoryToolItem;
import com.ruoyi.agent.tool.materialcategory.MaterialCategoryToolQuery;
import com.ruoyi.agent.tool.materialcategory.MaterialCategoryToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 物料分类查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/material-categories")
public class AgentMaterialCategoryToolController {
    private final MaterialCategoryToolService toolService;

    public AgentMaterialCategoryToolController(MaterialCategoryToolService toolService) {
        this.toolService = toolService;
    }

    /** 查询物料分类。 */
    @PostMapping("/query")
    public AgentToolResult<List<MaterialCategoryToolItem>> query(
            @RequestBody(required = false) MaterialCategoryToolQuery request) {
        return toolService.query(request);
    }
}
