package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.bom.BomStructureQuery;
import com.ruoyi.agent.tool.bom.BomStructureToolResult;
import com.ruoyi.agent.tool.bom.BomToolItem;
import com.ruoyi.agent.tool.bom.BomToolQuery;
import com.ruoyi.agent.tool.bom.BomToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify BOM 查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/boms")
public class AgentBomToolController {
    private final BomToolService toolService;

    public AgentBomToolController(BomToolService toolService) {
        this.toolService = toolService;
    }

    /** 查询 BOM 及版本摘要。 */
    @PostMapping("/query")
    public AgentToolResult<List<BomToolItem>> query(@RequestBody(required = false) BomToolQuery request) {
        return toolService.query(request);
    }

    /** 查询指定 BOM 版本的直接子件结构。 */
    @PostMapping("/structure")
    public AgentToolResult<BomStructureToolResult> structure(
            @RequestBody(required = false) BomStructureQuery request) {
        return toolService.structure(request);
    }
}
