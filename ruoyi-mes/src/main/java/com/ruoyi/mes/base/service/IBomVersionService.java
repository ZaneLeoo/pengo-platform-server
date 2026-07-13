package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.BomCheckResult;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.domain.BomVersionCompareResult;
import java.util.List;

/**
 * BOM版本业务接口。
 *
 * @author ruoyi
 */
public interface IBomVersionService {
    List<BomVersion> selectBomVersionList(BomVersion bomVersion);
    BomVersion selectBomVersionById(Long id);
    boolean checkVersionCodeUnique(BomVersion bomVersion);
    int insertBomVersion(BomVersion bomVersion);
    int updateBomVersion(BomVersion bomVersion);
    int deleteBomVersionByIds(Long[] ids);
    BomCheckResult checkBomVersion(Long id);
    BomVersionCompareResult compareBomVersion(Long baseVersionId, Long targetVersionId);

    /**
     * 复制BOM版本及子件明细。
     *
     * @param sourceVersionId
     *            源版本ID
     * @param targetVersionCode
     *            新版本号
     * @param targetVersionName
     *            新版本名称
     * @param createBy
     *            创建者
     */
    void copyBomVersion(Long sourceVersionId, String targetVersionCode, String targetVersionName, String createBy);
}
