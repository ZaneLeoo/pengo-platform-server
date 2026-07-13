package com.ruoyi.agent.tool.supplier;

import java.math.BigDecimal;

/** 供应商工具安全输出，不暴露联系方式和地址。 */
public record SupplierToolItem(Long id, String code, String name, String currency,
                               BigDecimal taxRate, String status)
{
}
