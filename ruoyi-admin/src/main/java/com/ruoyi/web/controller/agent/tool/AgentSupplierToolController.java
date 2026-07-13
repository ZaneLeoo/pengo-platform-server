package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.supplier.SupplierToolItem;
import com.ruoyi.agent.tool.supplier.SupplierToolQuery;
import com.ruoyi.agent.tool.supplier.SupplierToolService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 供应商查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools/suppliers")
public class AgentSupplierToolController {
    private final SupplierToolService toolService;

    public AgentSupplierToolController(SupplierToolService toolService) {
        this.toolService = toolService;
    }

    /** 查询供应商。 */
    @PostMapping("/query")
    public AgentToolResult<List<SupplierToolItem>> query(@RequestBody(required = false) SupplierToolQuery request) {
        return toolService.query(request);
    }
}
