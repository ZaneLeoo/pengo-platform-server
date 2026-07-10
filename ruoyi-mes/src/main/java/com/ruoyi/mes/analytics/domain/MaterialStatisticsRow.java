package com.ruoyi.mes.analytics.domain;

import lombok.Data;

/** 物料维度统计结果。 */
@Data
public class MaterialStatisticsRow
{
    private String dimensionCode;
    private String dimensionName;
    private Long materialCount;
}
