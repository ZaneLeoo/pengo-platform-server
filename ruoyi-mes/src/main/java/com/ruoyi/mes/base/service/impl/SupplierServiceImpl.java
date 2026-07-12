package com.ruoyi.mes.base.service.impl;

import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.mapper.SupplierMapper;
import com.ruoyi.mes.base.service.ISupplierService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements ISupplierService {

    private final SupplierMapper mapper;

    public SupplierServiceImpl(SupplierMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Supplier> selectList(Supplier q) { return mapper.selectList(q); }

    /** 查询面向 AI 工具的供应商最小候选集。 */
    @Override
    public List<Supplier> selectListForAgent(String keyword, String status) {
        return mapper.selectListForAgent(keyword, status);
    }

    @Override
    public Supplier selectById(Long id) { return mapper.selectById(id); }

    @Override
    public int insert(Supplier o) { return mapper.insert(o); }

    @Override
    public int update(Supplier o) { return mapper.update(o); }

    @Override
    public int deleteByIds(Long[] ids) { return mapper.deleteByIds(ids); }
}
