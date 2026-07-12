package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Supplier;
import java.util.List;

/** 供应商档案服务接口。 */
public interface ISupplierService {

    List<Supplier> selectList(Supplier q);

    Supplier selectById(Long id);

    int insert(Supplier o);

    int update(Supplier o);

    int deleteByIds(Long[] ids);
}
