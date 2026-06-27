package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.BomMaster;
import java.util.List;

/**
 * BOM主表业务接口。
 *
 * @author ruoyi
 */
public interface IBomMasterService {
    List<BomMaster> selectBomMasterList(BomMaster bomMaster);
    BomMaster selectBomMasterById(Long id);
    boolean checkBomCodeUnique(BomMaster bomMaster);
    int insertBomMaster(BomMaster bomMaster);
    int updateBomMaster(BomMaster bomMaster);
    int deleteBomMasterByIds(Long[] ids);
}
