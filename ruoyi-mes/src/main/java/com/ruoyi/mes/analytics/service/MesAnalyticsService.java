package com.ruoyi.mes.analytics.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsQuery;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsRow;
import com.ruoyi.mes.analytics.mapper.MesAnalyticsMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/** 为 Agent 提供显式白名单的 MES 只读统计能力。 */
@Service
public class MesAnalyticsService
{
    private final MesAnalyticsMapper analyticsMapper;

    public MesAnalyticsService(MesAnalyticsMapper analyticsMapper)
    {
        this.analyticsMapper = analyticsMapper;
    }

    /** 按受控维度统计物料数量。 */
    public List<MaterialStatisticsRow> materialStatistics(MaterialStatisticsQuery query)
    {
        if (query == null || query.getDimension() == null)
        {
            throw new ServiceException("物料统计维度不能为空");
        }
        return analyticsMapper.selectMaterialStatistics(query);
    }
}
