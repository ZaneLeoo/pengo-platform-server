package com.ruoyi.agent.tool.location;

import lombok.Data;

/** Dify 库位查询工具请求。 */
@Data
public class LocationToolQuery {
    private String locationCode;
    private String locationName;
    private Long warehouseId;
    private String warehouseCode;
    private Boolean includeDisabled = false;
}
