package com.ruoyi.mes.analytics.mapper;

import com.ruoyi.mes.analytics.domain.MaterialStatisticsQuery;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsRow;
import java.util.List;

/** MES 面向 Agent 的只读统计数据访问。 */
public interface MesAnalyticsMapper
{
    List<MaterialStatisticsRow> selectMaterialStatistics(MaterialStatisticsQuery query);
}
