package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomVersion;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * BOM版本数据访问接口。
 *
 * @author ruoyi
 */
public interface BomVersionMapper {
    List<BomVersion> selectBomVersionList(BomVersion bomVersion);
    BomVersion selectBomVersionById(Long id);
    BomVersion selectBomVersionByCode(@Param("bomMasterId") Long bomMasterId, @Param("versionCode") String versionCode);
    int resetDefaultFlag(@Param("bomMasterId") Long bomMasterId, @Param("excludeId") Long excludeId);
    int insertBomVersion(BomVersion bomVersion);
    int updateBomVersion(BomVersion bomVersion);
    int deleteBomVersionByIds(Long[] ids);
    int deleteBomVersionByMasterIds(Long[] bomMasterIds);
}
