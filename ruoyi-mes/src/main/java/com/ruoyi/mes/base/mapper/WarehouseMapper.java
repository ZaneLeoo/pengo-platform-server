package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Warehouse;
import java.util.List;

/** 仓库档案数据访问接口。 */
public interface WarehouseMapper {

    List<Warehouse> selectList(Warehouse q);

    Warehouse selectById(Long id);

    int insert(Warehouse o);

    int update(Warehouse o);

    int deleteByIds(Long[] ids);
}
