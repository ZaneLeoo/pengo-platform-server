package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomMaster;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * BOM主表数据访问接口。
 *
 * @author ruoyi
 */
public interface BomMasterMapper {
    /** 按 Agent 查询语义匹配 BOM 编码或母件信息。 */
    List<BomMaster> selectBomMasterListForAgent(@Param("keyword") String keyword,
            @Param("bomType") String bomType, @Param("status") String status);
    List<BomMaster> selectBomMasterList(BomMaster bomMaster);
    BomMaster selectBomMasterById(Long id);
    BomMaster selectBomMasterByCode(String bomCode);
    int insertBomMaster(BomMaster bomMaster);
    int updateBomMaster(BomMaster bomMaster);
    int deleteBomMasterByIds(Long[] ids);
}
