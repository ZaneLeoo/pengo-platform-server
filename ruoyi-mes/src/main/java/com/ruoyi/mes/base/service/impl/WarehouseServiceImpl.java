package com.ruoyi.mes.base.service.impl;

import com.ruoyi.mes.base.domain.Warehouse;
import com.ruoyi.mes.base.mapper.WarehouseMapper;
import com.ruoyi.mes.base.service.IWarehouseService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl implements IWarehouseService {

    private final WarehouseMapper mapper;

    public WarehouseServiceImpl(WarehouseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Warehouse> selectList(Warehouse q) { return mapper.selectList(q); }

    @Override
    public Warehouse selectById(Long id) { return mapper.selectById(id); }

    @Override
    public int insert(Warehouse o) { return mapper.insert(o); }

    @Override
    public int update(Warehouse o) { return mapper.update(o); }

    @Override
    public int deleteByIds(Long[] ids) { return mapper.deleteByIds(ids); }
}
