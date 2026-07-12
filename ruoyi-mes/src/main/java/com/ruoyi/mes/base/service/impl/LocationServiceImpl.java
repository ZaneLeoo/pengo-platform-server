package com.ruoyi.mes.base.service.impl;

import com.ruoyi.mes.base.domain.Location;
import com.ruoyi.mes.base.mapper.LocationMapper;
import com.ruoyi.mes.base.service.ILocationService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements ILocationService {

    private final LocationMapper mapper;

    public LocationServiceImpl(LocationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Location> selectList(Location q) { return mapper.selectList(q); }

    @Override
    public Location selectById(Long id) { return mapper.selectById(id); }

    @Override
    public int insert(Location o) { return mapper.insert(o); }

    @Override
    public int update(Location o) { return mapper.update(o); }

    @Override
    public int deleteByIds(Long[] ids) { return mapper.deleteByIds(ids); }
}
