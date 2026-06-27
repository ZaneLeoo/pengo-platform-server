package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * BOM子件明细数据访问接口。
 *
 * @author ruoyi
 */
public interface BomItemMapper {
    List<BomItem> selectBomItemList(BomItem bomItem);
    BomItem selectBomItemById(Long id);
    BomItem selectBomItemByLineNo(@Param("bomVersionId") Long bomVersionId, @Param("lineNo") Integer lineNo);
    int insertBomItem(BomItem bomItem);
    int updateBomItem(BomItem bomItem);
    int deleteBomItemByIds(Long[] ids);
    int deleteBomItemByVersionIds(Long[] bomVersionIds);
    int deleteBomItemByMasterIds(Long[] bomMasterIds);
}
