package com.ruoyi.mes.base.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 换算结果 DTO。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Data
public class ConversionResult {
    /** 单位编码 */
    private String unitCode;
    /** 单位名称 */
    private String unitName;
    /** 换算后的数量 */
    private BigDecimal quantity;
    /** 本次换算路径描述，如 "卷→平方米" */
    private String conversionPath;
}
