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
    /** 按父件编码懒加载子件（parentItemCode 为 null 时查顶层） */
    List<BomItem> selectBomItemChildren(@Param("bomVersionId") Long bomVersionId, @Param("parentItemCode") String parentItemCode);
    /** 跨BOM懒加载：按子件编码和版本查子件，versionId为空则取默认版本 */
    List<BomItem> selectBomItemByComponentCode(@Param("componentItemCode") String componentItemCode, @Param("bomVersionId") Long bomVersionId);
}
