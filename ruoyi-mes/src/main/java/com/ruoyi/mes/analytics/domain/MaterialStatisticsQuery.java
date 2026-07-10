package com.ruoyi.mes.analytics.domain;

import com.ruoyi.mes.analytics.enums.MaterialStatisticsDimension;
import lombok.Data;

/** Agent 物料统计查询，不接受原始 SQL。 */
@Data
public class MaterialStatisticsQuery
{
    private MaterialStatisticsDimension dimension;
    private String materialType;
    private Long categoryId;
    private String status;
}
