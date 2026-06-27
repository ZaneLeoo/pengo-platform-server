package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.mapper.BomItemMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import com.ruoyi.mes.base.service.IBomVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * BOM版本业务处理。
 *
 * @author ruoyi
 */
@Service
public class BomVersionServiceImpl implements IBomVersionService {
    @Autowired
    private BomVersionMapper bomVersionMapper;

    @Autowired
    private BomItemMapper bomItemMapper;

    @Override
    public List<BomVersion> selectBomVersionList(BomVersion bomVersion) {
        return bomVersionMapper.selectBomVersionList(bomVersion);
    }

    @Override
    public BomVersion selectBomVersionById(Long id) {
        return bomVersionMapper.selectBomVersionById(id);
    }

    @Override
    public boolean checkVersionCodeUnique(BomVersion bomVersion) {
        Long id = bomVersion.getId() == null ? 0L : bomVersion.getId();
        BomVersion info = bomVersionMapper.selectBomVersionByCode(bomVersion.getBomMasterId(), bomVersion.getVersionCode());
        return StringUtils.isNull(info) || info.getId().equals(id);
    }

    @Override
    public int insertBomVersion(BomVersion bomVersion) {
        validateDateRange(bomVersion);
        resetOtherDefaults(bomVersion);
        return bomVersionMapper.insertBomVersion(bomVersion);
    }

    @Override
    public int updateBomVersion(BomVersion bomVersion) {
        validateDateRange(bomVersion);
        resetOtherDefaults(bomVersion);
        return bomVersionMapper.updateBomVersion(bomVersion);
    }

    @Override
    public int deleteBomVersionByIds(Long[] ids) {
        bomItemMapper.deleteBomItemByVersionIds(ids);
        return bomVersionMapper.deleteBomVersionByIds(ids);
    }

    /**
     * 校验生效失效日期。
     *
     * @param bomVersion BOM版本
     */
    private void validateDateRange(BomVersion bomVersion) {
        if (bomVersion.getEffectiveDate() != null && bomVersion.getExpireDate() != null
                && bomVersion.getExpireDate().before(bomVersion.getEffectiveDate())) {
            throw new ServiceException("失效日期不能早于生效日期");
        }
    }

    /**
     * 保证同一BOM主表只有一个默认版本。
     *
     * @param bomVersion BOM版本
     */
    private void resetOtherDefaults(BomVersion bomVersion) {
        if (Integer.valueOf(1).equals(bomVersion.getDefaultFlag())) {
            bomVersionMapper.resetDefaultFlag(bomVersion.getBomMasterId(), bomVersion.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyBomVersion(Long sourceVersionId, String targetVersionCode, String targetVersionName, String createBy) {
        if (sourceVersionId == null || StringUtils.isEmpty(targetVersionCode)) {
            throw new ServiceException("源版本ID和目标版本号不能为空");
        }

        BomVersion sourceVersion = bomVersionMapper.selectBomVersionById(sourceVersionId);
        if (sourceVersion == null) {
            throw new ServiceException("未找到源版本信息");
        }

        // 校验唯一性
        BomVersion checkVersion = bomVersionMapper.selectBomVersionByCode(sourceVersion.getBomMasterId(), targetVersionCode);
        if (checkVersion != null) {
            throw new ServiceException("版本号'" + targetVersionCode + "'已存在");
        }

        // 创建新版本
        BomVersion targetVersion = new BomVersion();
        targetVersion.setBomMasterId(sourceVersion.getBomMasterId());
        targetVersion.setVersionCode(targetVersionCode);
        targetVersion.setVersionName(targetVersionName);
        targetVersion.setVersionDesc(sourceVersion.getVersionDesc());
        targetVersion.setBaseQty(sourceVersion.getBaseQty());
        targetVersion.setUsageType(sourceVersion.getUsageType());
        targetVersion.setEffectiveDate(sourceVersion.getEffectiveDate());
        targetVersion.setExpireDate(sourceVersion.getExpireDate());
        targetVersion.setStatus("DRAFT"); // 复制出的版本状态默认设为草稿
        targetVersion.setApproveStatus("UNAPPROVED"); // 审批状态默认为未审核
        targetVersion.setDefaultFlag(0); // 复制出的版本默认非默认版本
        targetVersion.setSourceSystem("MANUAL");
        targetVersion.setCreateBy(createBy);

        // 插入新版本
        bomVersionMapper.insertBomVersion(targetVersion);

        // 查询源版本的子件明细
        BomItem queryItem = new BomItem();
        queryItem.setBomVersionId(sourceVersionId);
        List<BomItem> items = bomItemMapper.selectBomItemList(queryItem);

        // 复制并插入子件明细
        if (items != null && !items.isEmpty()) {
            for (BomItem item : items) {
                item.setId(null);
                item.setBomVersionId(targetVersion.getId());
                item.setCreateBy(createBy);
                item.setCreateTime(null);
                item.setUpdateBy(null);
                item.setUpdateTime(null);
                bomItemMapper.insertBomItem(item);
            }
        }
    }
}
