package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.BomItem;
import java.util.List;

/**
 * BOM子件明细业务接口。
 *
 * @author ruoyi
 */
public interface IBomItemService {
    List<BomItem> selectBomItemList(BomItem bomItem);
    BomItem selectBomItemById(Long id);
    boolean checkLineNoUnique(BomItem bomItem);
    int insertBomItem(BomItem bomItem);
    int updateBomItem(BomItem bomItem);
    int deleteBomItemByIds(Long[] ids);
}
