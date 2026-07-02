package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomImportItem;
import java.util.List;

/** BOM OCR 导入草稿明细数据访问接口。 */
public interface BomImportItemMapper
{
    List<BomImportItem> selectBomImportItemList(BomImportItem item);
    int insertBomImportItem(BomImportItem item);
    int deleteBomImportItemByImportIds(Long[] importIds);
}
