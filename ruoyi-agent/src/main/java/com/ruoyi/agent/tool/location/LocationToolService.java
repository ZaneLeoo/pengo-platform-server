package com.ruoyi.agent.tool.location;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.Location;
import com.ruoyi.mes.base.service.ILocationService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 将库位档案服务适配为稳定的 Agent 工具协议。 */
@Service
public class LocationToolService {
    private static final String ENABLED_STATUS = "NORMAL";
    private static final int MAX_RESULT_SIZE = 50;
    private final ILocationService locationService;

    public LocationToolService(ILocationService locationService) {
        this.locationService = locationService;
    }

    /** 查询库位并限制模型上下文中的返回数量。 */
    public AgentToolResult<List<LocationToolItem>> query(LocationToolQuery request) {
        LocationToolQuery query = request == null ? new LocationToolQuery() : request;
        Location condition = new Location();
        condition.setLocationCode(query.getLocationCode());
        condition.setLocationName(query.getLocationName());
        condition.setWarehouseId(query.getWarehouseId());
        condition.setWarehouseCode(query.getWarehouseCode());
        condition.setStatus(Boolean.TRUE.equals(query.getIncludeDisabled()) ? null : ENABLED_STATUS);
        List<Location> matched = locationService.selectList(condition);
        List<LocationToolItem> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(matched.size(), matched.size() > MAX_RESULT_SIZE);
        if (data.isEmpty()) {
            return AgentToolResults.noResult(LocationToolResultCode.LOCATION_NOT_FOUND,
                    "未找到符合条件的库位", data, meta);
        }
        return AgentToolResults.success(LocationToolResultCode.LOCATION_QUERY_SUCCESS,
                "查询到 " + data.size() + " 个库位", data, meta);
    }

    private LocationToolItem toItem(Location location) {
        return new LocationToolItem(location.getId(), location.getLocationCode(), location.getLocationName(),
                location.getWarehouseId(), location.getWarehouseCode(), location.getWarehouseName(),
                location.getStatus());
    }
}
