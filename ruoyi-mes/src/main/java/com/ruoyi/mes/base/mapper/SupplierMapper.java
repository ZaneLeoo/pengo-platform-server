package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Supplier;
import java.util.List;

/** 供应商档案数据访问接口。 */
public interface SupplierMapper {

    List<Supplier> selectList(Supplier q);

    Supplier selectById(Long id);

    int insert(Supplier o);

    int update(Supplier o);

    int deleteByIds(Long[] ids);
}
