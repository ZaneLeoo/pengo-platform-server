package com.ruoyi.agent.tool.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 面向 Agent 的仓库安全输出字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseToolItem {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String manager;
    private String status;
}
