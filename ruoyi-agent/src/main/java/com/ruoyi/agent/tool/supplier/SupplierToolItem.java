package com.ruoyi.agent.tool.supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 供应商工具安全输出，不暴露联系方式和地址。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierToolItem {
    private Long id;
    private String code;
    private String name;
    private String currency;
    private BigDecimal taxRate;
    private String status;

}
