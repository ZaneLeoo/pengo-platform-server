package com.ruoyi.agent.tool.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 面向 Agent 的库位安全输出字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationToolItem {
    private Long id;
    private String code;
    private String name;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String status;
}
