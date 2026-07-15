package com.ruoyi.agent.tool.warehouse;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.Warehouse;
import com.ruoyi.mes.base.service.IWarehouseService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 将仓库档案服务适配为稳定的 Agent 工具协议。 */
@Service
public class WarehouseToolService {
    private static final String ENABLED_STATUS = "NORMAL";
    private static final int MAX_RESULT_SIZE = 50;
    private final IWarehouseService warehouseService;

    public WarehouseToolService(IWarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    /** 查询仓库并限制模型上下文中的返回数量。 */
    public AgentToolResult<List<WarehouseToolItem>> query(WarehouseToolQuery request) {
        WarehouseToolQuery query = request == null ? new WarehouseToolQuery() : request;
        Warehouse condition = new Warehouse();
        condition.setWarehouseCode(query.getWarehouseCode());
        condition.setWarehouseName(query.getWarehouseName());
        condition.setStatus(Boolean.TRUE.equals(query.getIncludeDisabled()) ? null : ENABLED_STATUS);
        List<Warehouse> matched = warehouseService.selectList(condition);
        List<WarehouseToolItem> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(matched.size(), matched.size() > MAX_RESULT_SIZE);
        if (data.isEmpty()) {
            return AgentToolResults.noResult(WarehouseToolResultCode.WAREHOUSE_NOT_FOUND,
                    "未找到符合条件的仓库", data, meta);
        }
        return AgentToolResults.success(WarehouseToolResultCode.WAREHOUSE_QUERY_SUCCESS,
                "查询到 " + data.size() + " 个仓库", data, meta);
    }

    private WarehouseToolItem toItem(Warehouse warehouse) {
        return new WarehouseToolItem(warehouse.getId(), warehouse.getWarehouseCode(), warehouse.getWarehouseName(),
                warehouse.getAddress(), warehouse.getManager(), warehouse.getStatus());
    }
}
