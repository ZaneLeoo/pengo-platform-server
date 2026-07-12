package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Location;
import java.util.List;

/** 库位档案服务接口。 */
public interface ILocationService {

    List<Location> selectList(Location q);

    Location selectById(Long id);

    int insert(Location o);

    int update(Location o);

    int deleteByIds(Long[] ids);
}
