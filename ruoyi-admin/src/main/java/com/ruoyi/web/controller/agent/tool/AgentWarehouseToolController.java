package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.location.LocationToolItem;
import com.ruoyi.agent.tool.location.LocationToolQuery;
import com.ruoyi.agent.tool.location.LocationToolService;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.warehouse.WarehouseToolItem;
import com.ruoyi.agent.tool.warehouse.WarehouseToolQuery;
import com.ruoyi.agent.tool.warehouse.WarehouseToolService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 仓库与库位查询工具 HTTP 入口。 */
@RestController
@RequestMapping("/agent/tools")
public class AgentWarehouseToolController {
    private final WarehouseToolService warehouseToolService;
    private final LocationToolService locationToolService;

    public AgentWarehouseToolController(WarehouseToolService warehouseToolService,
            LocationToolService locationToolService) {
        this.warehouseToolService = warehouseToolService;
        this.locationToolService = locationToolService;
    }

    /** 查询仓库档案。 */
    @PostMapping("/warehouses/query")
    public AgentToolResult<List<WarehouseToolItem>> queryWarehouses(
            @RequestBody(required = false) WarehouseToolQuery request) {
        return warehouseToolService.query(request);
    }

    /** 查询库位档案。 */
    @PostMapping("/locations/query")
    public AgentToolResult<List<LocationToolItem>> queryLocations(
            @RequestBody(required = false) LocationToolQuery request) {
        return locationToolService.query(request);
    }
}
