package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.mapper.BomItemMapper;
import com.ruoyi.mes.base.service.IBomItemService;
import com.ruoyi.mes.common.enums.SupplyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

/**
 * BOM子件明细业务处理。
 *
 * @author ruoyi
 */
@Service
public class BomItemServiceImpl implements IBomItemService {
    @Autowired
    private BomItemMapper bomItemMapper;

    @Override
    public List<BomItem> selectBomItemList(BomItem bomItem) {
        return bomItemMapper.selectBomItemList(bomItem);
    }

    @Override
    public BomItem selectBomItemById(Long id) {
        return bomItemMapper.selectBomItemById(id);
    }

    @Override
    public boolean checkLineNoUnique(BomItem bomItem) {
        Long id = bomItem.getId() == null ? 0L : bomItem.getId();
        BomItem info = bomItemMapper.selectBomItemByLineNo(bomItem.getBomVersionId(), bomItem.getLineNo());
        return StringUtils.isNull(info) || info.getId().equals(id);
    }

    @Override
    public int insertBomItem(BomItem bomItem) {
        prepareDefaults(bomItem);
        return bomItemMapper.insertBomItem(bomItem);
    }

    @Override
    public int updateBomItem(BomItem bomItem) {
        prepareDefaults(bomItem);
        return bomItemMapper.updateBomItem(bomItem);
    }

    @Override
    public int deleteBomItemByIds(Long[] ids) {
        return bomItemMapper.deleteBomItemByIds(ids);
    }

    @Override
    public List<BomItem> selectBomItemChildren(Long bomVersionId, String parentItemCode) {
        return bomItemMapper.selectBomItemChildren(bomVersionId, parentItemCode);
    }

    @Override
    public List<BomItem> selectBomItemByComponentCode(String componentItemCode, Long bomVersionId) {
        return bomItemMapper.selectBomItemByComponentCode(componentItemCode, bomVersionId);
    }

    /**
     * 准备默认值与派生字段。
     *
     * @param bomItem
     *            BOM明细
     */
    private void prepareDefaults(BomItem bomItem) {
        if (bomItem.getFixedLossQty() == null) {
            bomItem.setFixedLossQty(BigDecimal.ZERO);
        }
        if (bomItem.getChangeLossRate() == null) {
            bomItem.setChangeLossRate(BigDecimal.ZERO);
        }
        if (SupplyType.VIRTUAL.getCode().equals(bomItem.getSupplyType())) {
            bomItem.setIsVirtual(1);
        }
    }
}
