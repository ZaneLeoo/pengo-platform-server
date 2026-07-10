package com.ruoyi.mes.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsQuery;
import com.ruoyi.mes.analytics.domain.MaterialStatisticsRow;
import com.ruoyi.mes.analytics.enums.MaterialStatisticsDimension;
import com.ruoyi.mes.analytics.mapper.MesAnalyticsMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class MesAnalyticsServiceTest
{
    @Test
    void shouldUseWhitelistedDimension()
    {
        CapturingMapper mapper = new CapturingMapper();
        MesAnalyticsService service = new MesAnalyticsService(mapper);
        MaterialStatisticsQuery query = new MaterialStatisticsQuery();
        query.setDimension(MaterialStatisticsDimension.CATEGORY);

        List<MaterialStatisticsRow> rows = service.materialStatistics(query);

        assertEquals(MaterialStatisticsDimension.CATEGORY, mapper.query.getDimension());
        assertEquals(1, rows.size());
    }

    @Test
    void shouldRejectMissingDimension()
    {
        MesAnalyticsService service = new MesAnalyticsService(query -> List.of());
        assertThrows(ServiceException.class, () -> service.materialStatistics(new MaterialStatisticsQuery()));
    }

    private static class CapturingMapper implements MesAnalyticsMapper
    {
        private MaterialStatisticsQuery query;

        @Override
        public List<MaterialStatisticsRow> selectMaterialStatistics(MaterialStatisticsQuery query)
        {
            this.query = query;
            MaterialStatisticsRow row = new MaterialStatisticsRow();
            row.setDimensionCode("1");
            row.setDimensionName("成品");
            row.setMaterialCount(8L);
            return List.of(row);
        }
    }
}
