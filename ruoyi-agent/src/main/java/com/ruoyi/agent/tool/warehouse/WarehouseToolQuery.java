package com.ruoyi.agent.tool.warehouse;

import lombok.Data;

/** Dify 仓库查询工具请求。 */
@Data
public class WarehouseToolQuery {
    private String warehouseCode;
    private String warehouseName;
    private Boolean includeDisabled = false;
}
