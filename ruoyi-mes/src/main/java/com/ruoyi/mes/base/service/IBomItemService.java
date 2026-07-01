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
    /** 按父件编码懒加载子件列表（树形结构） */
    List<BomItem> selectBomItemChildren(Long bomVersionId, String parentItemCode);
    /** 跨BOM懒加载：按子件编码和版本查子件，versionId为空则取默认版本 */
    List<BomItem> selectBomItemByComponentCode(String componentItemCode, Long bomVersionId);
}
