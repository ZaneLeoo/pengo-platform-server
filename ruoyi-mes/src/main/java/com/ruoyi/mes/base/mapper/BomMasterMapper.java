package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomMaster;
import java.util.List;

/**
 * BOM主表数据访问接口。
 *
 * @author ruoyi
 */
public interface BomMasterMapper {
    List<BomMaster> selectBomMasterList(BomMaster bomMaster);
    BomMaster selectBomMasterById(Long id);
    BomMaster selectBomMasterByCode(String bomCode);
    int insertBomMaster(BomMaster bomMaster);
    int updateBomMaster(BomMaster bomMaster);
    int deleteBomMasterByIds(Long[] ids);
}
