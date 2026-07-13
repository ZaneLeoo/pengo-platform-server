package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomCheckIssue;
import com.ruoyi.mes.base.domain.BomCheckResult;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.domain.BomVersionCompareItem;
import com.ruoyi.mes.base.domain.BomVersionCompareResult;
import com.ruoyi.mes.base.mapper.BomItemMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import com.ruoyi.mes.base.service.IBomVersionService;
import com.ruoyi.mes.common.enums.BomApproveStatus;
import com.ruoyi.mes.common.enums.BomVersionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        BomVersion info = bomVersionMapper.selectBomVersionByCode(bomVersion.getBomMasterId(),
                bomVersion.getVersionCode());
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

    @Override
    public BomCheckResult checkBomVersion(Long id) {
        BomVersion version = bomVersionMapper.selectBomVersionById(id);
        if (version == null) {
            throw new ServiceException("未找到BOM版本信息");
        }

        BomCheckResult result = new BomCheckResult();
        result.setBomVersionId(version.getId());
        result.setVersionCode(version.getVersionCode());
        result.setStatus(version.getStatus());
        result.setApproveStatus(version.getApproveStatus());

        BomItem query = new BomItem();
        query.setBomVersionId(id);
        List<BomItem> items = bomItemMapper.selectBomItemList(query);
        if (items == null || items.isEmpty()) {
            result.addIssue(new BomCheckIssue("ERROR", "NO_ITEMS", "当前版本未维护任何子件，不能投产"));
        } else {
            appendItemIssues(result, items);
        }

        if (BomVersionStatus.DRAFT.getCode().equals(version.getStatus())) {
            result.addIssue(new BomCheckIssue("INFO", "VERSION_DRAFT", "当前版本仍为草稿，投产前需要先生效"));
        }
        if (BomVersionStatus.FROZEN.getCode().equals(version.getStatus())) {
            result.addIssue(new BomCheckIssue("INFO", "VERSION_FROZEN", "当前版本已冻结，建议复制新版本后再调整"));
        }
        return result;
    }

    @Override
    public BomVersionCompareResult compareBomVersion(Long baseVersionId, Long targetVersionId) {
        if (Objects.equals(baseVersionId, targetVersionId)) {
            throw new ServiceException("请选择两个不同的BOM版本进行对比");
        }

        BomVersion baseVersion = bomVersionMapper.selectBomVersionById(baseVersionId);
        BomVersion targetVersion = bomVersionMapper.selectBomVersionById(targetVersionId);
        if (baseVersion == null || targetVersion == null) {
            throw new ServiceException("未找到BOM版本信息");
        }
        if (!Objects.equals(baseVersion.getBomMasterId(), targetVersion.getBomMasterId())) {
            throw new ServiceException("只能对比同一BOM主表下的版本");
        }

        BomVersionCompareResult result = new BomVersionCompareResult();
        result.setBaseVersionId(baseVersion.getId());
        result.setBaseVersionCode(baseVersion.getVersionCode());
        result.setTargetVersionId(targetVersion.getId());
        result.setTargetVersionCode(targetVersion.getVersionCode());

        Map<String, BomItem> baseItems = loadVersionItems(baseVersionId);
        Map<String, BomItem> targetItems = loadVersionItems(targetVersionId);

        for (Map.Entry<String, BomItem> entry : targetItems.entrySet()) {
            BomItem targetItem = entry.getValue();
            BomItem baseItem = baseItems.get(entry.getKey());
            if (baseItem == null) {
                result.addDifference(buildCompareItem("ADD", targetItem, "component", "子件", null,
                        targetItem.getComponentItemCode()));
            } else {
                appendChangedFields(result, baseItem, targetItem);
            }
        }
        for (Map.Entry<String, BomItem> entry : baseItems.entrySet()) {
            if (!targetItems.containsKey(entry.getKey())) {
                BomItem baseItem = entry.getValue();
                result.addDifference(
                        buildCompareItem("DELETE", baseItem, "component", "子件", baseItem.getComponentItemCode(), null));
            }
        }
        return result;
    }

    /**
     * 校验生效失效日期。
     *
     * @param bomVersion
     *            BOM版本
     */
    private void validateDateRange(BomVersion bomVersion) {
        if (bomVersion.getEffectiveDate() != null && bomVersion.getExpireDate() != null
                && bomVersion.getExpireDate().before(bomVersion.getEffectiveDate())) {
            throw new ServiceException("失效日期不能早于生效日期");
        }
    }

    private void appendItemIssues(BomCheckResult result, List<BomItem> items) {
        Map<String, BomItem> componentMap = new HashMap<>();
        for (BomItem item : items) {
            if (StringUtils.isEmpty(item.getComponentItemCode()) || StringUtils.isEmpty(item.getComponentItemName())) {
                result.addIssue(buildItemIssue("ERROR", "MISSING_COMPONENT", "存在未完整维护物料编码或名称的子件", item));
            }
            if (item.getComponentQty() == null || BigDecimal.ZERO.compareTo(item.getComponentQty()) >= 0) {
                result.addIssue(buildItemIssue("ERROR", "INVALID_QTY", "子件用量必须大于0", item));
            }
            if (StringUtils.isEmpty(item.getSupplyType())) {
                result.addIssue(buildItemIssue("ERROR", "MISSING_SUPPLY_TYPE", "子件未维护发料方式", item));
            }

            String duplicateKey = (item.getParentItemCode() == null ? "" : item.getParentItemCode())
                    + "|" + item.getComponentItemCode();
            if (!StringUtils.isEmpty(item.getComponentItemCode()) && componentMap.containsKey(duplicateKey)) {
                result.addIssue(buildItemIssue("WARN", "DUPLICATE_COMPONENT", "同一父件下存在重复子件，请确认是否需要合并用量", item));
            } else {
                componentMap.put(duplicateKey, item);
            }
        }
    }

    private Map<String, BomItem> loadVersionItems(Long bomVersionId) {
        BomItem query = new BomItem();
        query.setBomVersionId(bomVersionId);
        List<BomItem> items = bomItemMapper.selectBomItemList(query);
        Map<String, BomItem> itemMap = new LinkedHashMap<>();
        if (items == null) {
            return itemMap;
        }
        for (BomItem item : items) {
            itemMap.put(compareKey(item), item);
        }
        return itemMap;
    }

    private String compareKey(BomItem item) {
        return (item.getParentItemCode() == null ? "" : item.getParentItemCode())
                + "|" + item.getComponentItemCode();
    }

    private void appendChangedFields(BomVersionCompareResult result, BomItem baseItem, BomItem targetItem) {
        addChangeIfNeeded(result, baseItem, targetItem, "componentQty", "用量",
                valueOf(baseItem.getComponentQty()), valueOf(targetItem.getComponentQty()));
        addChangeIfNeeded(result, baseItem, targetItem, "fixedLossQty", "固定损耗",
                valueOf(baseItem.getFixedLossQty()), valueOf(targetItem.getFixedLossQty()));
        addChangeIfNeeded(result, baseItem, targetItem, "changeLossRate", "变动损耗率",
                valueOf(baseItem.getChangeLossRate()), valueOf(targetItem.getChangeLossRate()));
        addChangeIfNeeded(result, baseItem, targetItem, "supplyType", "发料方式",
                baseItem.getSupplyType(), targetItem.getSupplyType());
        addChangeIfNeeded(result, baseItem, targetItem, "isVirtual", "虚拟件",
                valueOf(baseItem.getIsVirtual()), valueOf(targetItem.getIsVirtual()));
        addChangeIfNeeded(result, baseItem, targetItem, "mrpExpandFlag", "MRP展开",
                valueOf(baseItem.getMrpExpandFlag()), valueOf(targetItem.getMrpExpandFlag()));
    }

    private void addChangeIfNeeded(BomVersionCompareResult result, BomItem baseItem, BomItem targetItem,
            String fieldName, String fieldLabel, String baseValue, String targetValue) {
        if (!Objects.equals(baseValue, targetValue)) {
            result.addDifference(buildCompareItem("CHANGE", targetItem, fieldName, fieldLabel, baseValue, targetValue));
        }
    }

    private BomVersionCompareItem buildCompareItem(String diffType, BomItem item, String fieldName,
            String fieldLabel, String baseValue, String targetValue) {
        BomVersionCompareItem compareItem = new BomVersionCompareItem();
        compareItem.setDiffType(diffType);
        compareItem.setParentItemCode(item.getParentItemCode());
        compareItem.setComponentItemCode(item.getComponentItemCode());
        compareItem.setComponentItemName(item.getComponentItemName());
        compareItem.setFieldName(fieldName);
        compareItem.setFieldLabel(fieldLabel);
        compareItem.setBaseValue(baseValue);
        compareItem.setTargetValue(targetValue);
        return compareItem;
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BomCheckIssue buildItemIssue(String level, String code, String message, BomItem item) {
        BomCheckIssue issue = new BomCheckIssue(level, code, message);
        issue.setItemId(item.getId());
        issue.setLineNo(item.getLineNo());
        issue.setComponentItemCode(item.getComponentItemCode());
        return issue;
    }

    /**
     * 保证同一BOM主表只有一个默认版本。
     *
     * @param bomVersion
     *            BOM版本
     */
    private void resetOtherDefaults(BomVersion bomVersion) {
        if (Integer.valueOf(1).equals(bomVersion.getDefaultFlag())) {
            BomVersion versionForDefault = resolveVersionForDefault(bomVersion);
            if (!BomVersionStatus.EFFECTIVE.getCode().equals(versionForDefault.getStatus())
                    || !BomApproveStatus.APPROVED.getCode().equals(versionForDefault.getApproveStatus())) {
                throw new ServiceException("只有已审核且生效的BOM版本才能设为默认版本");
            }
            bomVersionMapper.resetDefaultFlag(bomVersion.getBomMasterId(), bomVersion.getId());
        }
    }

    /**
     * 获取用于默认版本校验的完整版本信息。
     *
     * @param bomVersion
     *            BOM版本
     * @return 完整BOM版本
     */
    private BomVersion resolveVersionForDefault(BomVersion bomVersion) {
        if (bomVersion.getId() == null) {
            return bomVersion;
        }
        BomVersion current = bomVersionMapper.selectBomVersionById(bomVersion.getId());
        if (current == null) {
            throw new ServiceException("未找到BOM版本信息");
        }
        if (bomVersion.getBomMasterId() == null) {
            bomVersion.setBomMasterId(current.getBomMasterId());
        }
        if (bomVersion.getStatus() != null) {
            current.setStatus(bomVersion.getStatus());
        }
        if (bomVersion.getApproveStatus() != null) {
            current.setApproveStatus(bomVersion.getApproveStatus());
        }
        return current;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyBomVersion(Long sourceVersionId, String targetVersionCode, String targetVersionName,
            String createBy) {
        if (sourceVersionId == null || StringUtils.isEmpty(targetVersionCode)) {
            throw new ServiceException("源版本ID和目标版本号不能为空");
        }

        BomVersion sourceVersion = bomVersionMapper.selectBomVersionById(sourceVersionId);
        if (sourceVersion == null) {
            throw new ServiceException("未找到源版本信息");
        }

        // 校验唯一性
        BomVersion checkVersion = bomVersionMapper.selectBomVersionByCode(sourceVersion.getBomMasterId(),
                targetVersionCode);
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
        targetVersion.setApproveStatus(BomApproveStatus.PENDING.getCode()); // 审批状态默认为待审核
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
