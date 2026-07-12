package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Warehouse;
import java.util.List;

/** 仓库档案服务接口。 */
public interface IWarehouseService {

    List<Warehouse> selectList(Warehouse q);

    Warehouse selectById(Long id);

    int insert(Warehouse o);

    int update(Warehouse o);

    int deleteByIds(Long[] ids);
}
