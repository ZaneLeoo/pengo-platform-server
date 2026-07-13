package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.material.MaterialToolItem;
import com.ruoyi.agent.tool.material.MaterialToolQuery;
import com.ruoyi.agent.tool.material.MaterialToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 物料查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/materials")
public class AgentMaterialToolController {
    private final MaterialToolService toolService;

    public AgentMaterialToolController(MaterialToolService toolService) {
        this.toolService = toolService;
    }

    /** 查询物料。 */
    @PostMapping("/query")
    public AgentToolResult<List<MaterialToolItem>> query(@RequestBody(required = false) MaterialToolQuery request) {
        return toolService.query(request);
    }
}
