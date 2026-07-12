package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Supplier;
import java.util.List;

/** 供应商档案服务接口。 */
public interface ISupplierService {

    List<Supplier> selectList(Supplier q);

    /** 查询面向 AI 工具的供应商最小候选集。 */
    List<Supplier> selectListForAgent(String keyword, String status);

    Supplier selectById(Long id);

    int insert(Supplier o);

    int update(Supplier o);

    int deleteByIds(Long[] ids);
}
