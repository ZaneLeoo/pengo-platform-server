package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Location;
import java.util.List;

/** 库位档案数据访问接口。 */
public interface LocationMapper {

    List<Location> selectList(Location q);

    Location selectById(Long id);

    int insert(Location o);

    int update(Location o);

    int deleteByIds(Long[] ids);
}
