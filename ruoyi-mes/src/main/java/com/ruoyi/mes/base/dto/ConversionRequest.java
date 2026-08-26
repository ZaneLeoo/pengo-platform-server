package com.ruoyi.mes.base.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

/**
 * 换算请求 DTO。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Data
public class ConversionRequest {
    /** 物料ID */
    private Long materialId;

    /** 输入单位编码 */
    private String inputUnitCode;

    /** 输入数量 */
    private BigDecimal inputQuantity;

    /** 运行时覆盖值（如称重数据），可选 */
    private Map<String, BigDecimal> runtimeOverrides;
}
