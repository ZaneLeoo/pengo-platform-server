package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.mapper.BomItemMapper;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import com.ruoyi.mes.base.service.IBomMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * BOM主表业务处理。
 *
 * @author ruoyi
 */
@Service
public class BomMasterServiceImpl implements IBomMasterService {
    @Autowired
    private BomMasterMapper bomMasterMapper;

    @Autowired
    private BomVersionMapper bomVersionMapper;

    @Autowired
    private BomItemMapper bomItemMapper;

    @Override
    public List<BomMaster> selectBomMasterList(BomMaster bomMaster) {
        return bomMasterMapper.selectBomMasterList(bomMaster);
    }

    @Override
    public BomMaster selectBomMasterById(Long id) {
        return bomMasterMapper.selectBomMasterById(id);
    }

    @Override
    public boolean checkBomCodeUnique(BomMaster bomMaster) {
        Long id = bomMaster.getId() == null ? 0L : bomMaster.getId();
        BomMaster info = bomMasterMapper.selectBomMasterByCode(bomMaster.getBomCode());
        return StringUtils.isNull(info) || info.getId().equals(id);
    }

    @Override
    public int insertBomMaster(BomMaster bomMaster) {
        return bomMasterMapper.insertBomMaster(bomMaster);
    }

    @Override
    public int updateBomMaster(BomMaster bomMaster) {
        return bomMasterMapper.updateBomMaster(bomMaster);
    }

    @Override
    public int deleteBomMasterByIds(Long[] ids) {
        bomItemMapper.deleteBomItemByMasterIds(ids);
        bomVersionMapper.deleteBomVersionByMasterIds(ids);
        return bomMasterMapper.deleteBomMasterByIds(ids);
    }
}
